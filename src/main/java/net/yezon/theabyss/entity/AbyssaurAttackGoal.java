package net.yezon.theabyss.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.network.NightbladeParryCueMessage;
import net.yezon.theabyss.network.NightbladeShakeMessage;

public class AbyssaurAttackGoal extends Goal {
    enum Attack {
        BITE("bite", 8, 18),
        LUNGE("lunge", 8, 8 + 26),
        TAIL("tail_sweep", 14, 28),
        ROAR("roar", 12, 40),
        BREATH("breath", 13, 36),
        STAGGER("stagger", -1, 30);

        final String animation;
        final int impact;
        final int length;

        Attack(String animation, int impact, int length) {
            this.animation = animation;
            this.impact = impact;
            this.length = length;
        }
    }

    private static final double BITE_REACH = 4.2;
    private static final double TAIL_RADIUS = 6.0;
    private static final double ROAR_RADIUS = 16.0;
    private static final double BREATH_RANGE = 9.0;
    private static final double BREATH_HALF_ANGLE = Math.toRadians(28.0);
    private static final int BREATH_END = 27;
    private static final double LUNGE_SPEED = 0.85;
    private static final int LUNGE_TICKS = 16;
    private static final double POOL_RADIUS = 1.6;

    private final AbyssaurEntity dino;
    private Attack attack;
    private int attackTick;
    private int cooldown;
    private int lungeCooldown = 60;
    private int tailCooldown = 20;
    private int roarCooldown = 160;
    private int breathCooldown = 80;
    private int repath;
    private Vec3 pathedTo;
    private static final float ATTACK_TURN = 8.0F;
    private LivingEntity attackTarget;
    private Vec3 lungeDir = Vec3.ZERO;
    private boolean lungeDone;

    private record Pool(Vec3 at, long until) {
    }

    private final List<Pool> pools = new ArrayList<>();
    boolean devHold;

    public AbyssaurAttackGoal(AbyssaurEntity dino) {
        this.dino = dino;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.dino.getTarget();
        return target != null && target.isAlive() || !this.pools.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.attack != null || this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.attack = null;
        this.dino.getNavigation().stop();
    }

    private float damage(float factor) {
        return AbyssaurEntity.halfHearts((float)this.dino.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor);
    }

    @Override
    public void tick() {
        if (!(this.dino.level() instanceof ServerLevel level)) {
            return;
        }
        this.cooldown--;
        this.lungeCooldown--;
        this.tailCooldown--;
        this.roarCooldown--;
        this.breathCooldown--;
        this.tickPools(level);
        if (this.attack != null) {
            this.tickAttack(level);
            return;
        }
        LivingEntity target = this.dino.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.dino.getLookControl().setLookAt(target, 20.0F, 20.0F);
        if (this.devHold) {
            this.dino.getNavigation().stop();
            return;
        }
        boolean moved = this.pathedTo == null || this.pathedTo.distanceToSqr(target.position()) > 2.5 * 2.5;
        if (--this.repath <= 0 && moved || this.dino.getNavigation().isDone()) {
            this.repath = 20 + this.dino.getRandom().nextInt(10);
            this.pathedTo = target.position();
            this.dino.getNavigation().moveTo(target, 1.2);
        }
        if (this.cooldown <= 0 && this.dino.getSensing().hasLineOfSight(target)) {
            Attack next = this.choose(level, target);
            if (next != null) {
                this.begin(next, target);
            }
        }
    }

    Vec3 forward() {
        float yaw = this.dino.getYRot() * ((float)Math.PI / 180.0F);
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private double reachTo(LivingEntity target) {
        double dx = target.getX() - this.dino.getX();
        double dz = target.getZ() - this.dino.getZ();
        return Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
    }

    private double facing(LivingEntity e) {
        Vec3 to = new Vec3(e.getX() - this.dino.getX(), 0.0, e.getZ() - this.dino.getZ());
        return to.lengthSqr() < 1.0E-4 ? 1.0 : to.normalize().dot(this.forward());
    }

    private void face(Vec3 dir) {
        if (dir.horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        float want = (float)(Math.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        float start = this.dino.tickStartYaw;
        float yaw = start + net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.wrapDegrees(want - start), -ATTACK_TURN, ATTACK_TURN);
        this.dino.setYRot(yaw);
        this.dino.setYBodyRot(yaw);
        this.dino.setYHeadRot(yaw);
    }

    private Vec3 jaws() {
        return this.dino.position().add(this.forward().scale(2.8)).add(0.0, 1.9, 0.0);
    }

    private boolean strike(ServerLevel level, LivingEntity victim, float amount, Vec3 velocity) {
        if (!victim.hurtServer(level, this.dino.damageSources().mobAttack(this.dino), amount)) {
            return false;
        }
        if (velocity != null) {
            victim.setDeltaMovement(velocity);
            net.yezon.theabyss.util.FallGuard.protect(victim, 80);
            victim.needsSync = true;
            victim.syncVelocity = true;
        }
        return true;
    }

    boolean hittable(LivingEntity e) {
        if (e == this.dino || !e.isAlive()) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(e instanceof Monster) || e == this.dino.getTarget();
    }

    private Attack choose(ServerLevel level, LivingEntity target) {
        double reach = this.reachTo(target);
        double dy = Math.abs(target.getY() - this.dino.getY());
        double front = this.facing(target);
        int[] w = new int[Attack.values().length];
        if (dy <= 3.0) {
            w[Attack.BITE.ordinal()] = reach <= BITE_REACH && front > 0.4 ? 50 : 0;
            if (this.tailCooldown <= 0) {
                boolean flank = !level.getEntitiesOfClass(LivingEntity.class, this.dino.getBoundingBox().inflate(TAIL_RADIUS, 2.0, TAIL_RADIUS),
                    e -> this.hittable(e) && this.facing(e) < 0.3 && e.distanceTo(this.dino) <= TAIL_RADIUS).isEmpty();
                w[Attack.TAIL.ordinal()] = flank ? 70 : reach <= 3.0 ? 12 : 0;
            }
            if (reach >= 3.5 && reach <= BREATH_RANGE && this.breathCooldown <= 0) {
                w[Attack.BREATH.ordinal()] = 35;
            }
        }
        if (dy <= 2.0 && reach >= 6.0 && reach <= 14.0 && this.lungeCooldown <= 0) {
            w[Attack.LUNGE.ordinal()] = 35;
        }
        if (reach <= ROAR_RADIUS && this.roarCooldown <= 0) {
            w[Attack.ROAR.ordinal()] = 15 + (this.dino.getHealth() < this.dino.getMaxHealth() * 0.5F ? 30 : 0);
        }
        int total = 0;
        for (int v : w) {
            total += v;
        }
        if (total <= 0) {
            return null;
        }
        int roll = this.dino.getRandom().nextInt(total);
        for (Attack a : Attack.values()) {
            roll -= w[a.ordinal()];
            if (roll < 0) {
                return a;
            }
        }
        return null;
    }

    void devStop() {
        this.attack = null;
        this.dino.getNavigation().stop();
        this.dino.setDeltaMovement(Vec3.ZERO);
    }

    void devForce(String name, LivingEntity target) {
        for (Attack a : Attack.values()) {
            if (a.impact >= 0 && a.animation.equals(name)) {
                this.dino.setTarget(target);
                this.begin(a, target);
                return;
            }
        }
    }

    private void begin(Attack next, LivingEntity target) {
        this.attack = next;
        this.attackTick = 0;
        this.attackTarget = target;
        this.dino.getNavigation().stop();
        this.dino.triggerAnim("action", next.animation);
        ServerLevel level = (ServerLevel)this.dino.level();
        switch (next) {
            case LUNGE -> {
                this.lungeCooldown = 160 + this.dino.getRandom().nextInt(80);
                this.lungeDone = false;
            }
            case TAIL -> this.tailCooldown = 60 + this.dino.getRandom().nextInt(40);
            case ROAR -> this.roarCooldown = 500 + this.dino.getRandom().nextInt(200);
            case BREATH -> {
                this.breathCooldown = 180 + this.dino.getRandom().nextInt(80);
                AbyssaurFx.breathStart(level, this.jaws());
            }
            default -> {
            }
        }
    }

    private void stagger(ServerLevel level) {
        this.attack = Attack.STAGGER;
        this.attackTick = 0;
        this.dino.triggerAnim("action", Attack.STAGGER.animation);
        AbyssaurFx.stagger(level, this.dino);
    }

    private void tickAttack(ServerLevel level) {
        Attack current = this.attack;
        this.attackTick++;
        LivingEntity target = this.attackTarget;
        boolean aim = target != null && target.isAlive();
        this.dino.getNavigation().stop();
        switch (current) {
            case BITE -> {
                if (aim && this.attackTick < current.impact - 2) {
                    this.face(target.position().subtract(this.dino.position()));
                }
                if (this.attackTick == current.impact) {
                    boolean hit = aim && this.reachTo(target) <= BITE_REACH && this.facing(target) > 0.3
                        && Math.abs(target.getY() - this.dino.getY()) < 3.0 && this.strike(level, target, this.damage(1.0F), null);
                    AbyssaurFx.bite(level, this.jaws(), hit);
                }
            }
            case LUNGE -> this.tickLunge(level, target);
            case TAIL -> {
                if (this.attackTick == current.impact - 4) {
                    AbyssaurFx.tailWhoosh(level, this.dino);
                }
                if (this.attackTick == current.impact) {
                    this.tailSweep(level);
                }
            }
            case ROAR -> {
                if (aim) {
                    this.face(target.position().subtract(this.dino.position()));
                }
                if (this.attackTick == current.impact) {
                    this.roar(level);
                }
            }
            case BREATH -> {
                if (aim && this.attackTick < current.impact) {
                    this.face(target.position().subtract(this.dino.position()));
                }
                if (this.attackTick >= current.impact && this.attackTick <= BREATH_END) {
                    this.breath(level);
                }
            }
            default -> {
            }
        }
        if (this.attack == current && this.attackTick >= current.length) {
            this.attack = null;
            this.cooldown = 10 + this.dino.getRandom().nextInt(12);
        }
    }

    private void tickLunge(ServerLevel level, LivingEntity target) {
        int t = this.attackTick - Attack.LUNGE.impact;
        boolean aim = target != null && target.isAlive();
        if (t < 0) {
            if (aim) {
                this.face(target.position().subtract(this.dino.position()));
            }
            return;
        }
        if (t == 0) {
            this.lungeDir = this.forward();
            this.dino.setDeltaMovement(this.lungeDir.x * LUNGE_SPEED, 0.35, this.lungeDir.z * LUNGE_SPEED);
            AbyssaurFx.lungeJump(level, this.dino);
        }
        if (this.lungeDone || t > LUNGE_TICKS) {
            return;
        }
        this.face(this.lungeDir);
        this.dino.setDeltaMovement(this.lungeDir.x * LUNGE_SPEED, this.dino.getDeltaMovement().y, this.lungeDir.z * LUNGE_SPEED);
        AbyssaurFx.lungeTrail(level, this.dino);
        if (aim && target instanceof ServerPlayer player && (t == 0 || t % 2 == 0)) {
            double gap = Math.max(0.0, this.reachTo(target) - (BITE_REACH - 0.6));
            NightbladeParryCueMessage.send(player, this.dino.getId(), (int)Math.ceil(gap / LUNGE_SPEED), NightbladeParry.MINI_BOSS_WINDOW);
        }
        boolean reached = aim && this.reachTo(target) <= BITE_REACH - 0.6 && this.facing(target) > 0.2 && Math.abs(target.getY() - this.dino.getY()) < 3.0;
        if (reached || t == LUNGE_TICKS) {
            this.lungeDone = true;
            this.dino.setDeltaMovement(this.dino.getDeltaMovement().multiply(0.2, 1.0, 0.2));
            if (!reached) {
                AbyssaurFx.bite(level, this.jaws(), false);
                return;
            }
            if (target instanceof ServerPlayer player && NightbladeParry.consume(player, NightbladeParry.MINI_BOSS_WINDOW)) {
                Vec3 at = target.position().add(0.0, 1.3, 0.0);
                NightbladeParry.playParryEffect(level, at);
                NightbladeImpactMessage.send(player, true);
                this.dino.setDeltaMovement(this.lungeDir.scale(-0.5).add(0.0, 0.2, 0.0));
                this.stagger(level);
                return;
            }
            boolean hit = this.strike(level, target, this.damage(1.0F), this.lungeDir.scale(0.9).add(0.0, 0.45, 0.0));
            AbyssaurFx.bite(level, this.jaws(), hit);
            if (hit) {
                NightbladeShakeMessage.around(level, target.position(), 10.0, 0.8F, 0.3F);
            }
        }
    }

    private void tailSweep(ServerLevel level) {
        AbyssaurFx.tailArc(level, this.dino.position(), this.forward(), TAIL_RADIUS);
        NightbladeShakeMessage.around(level, this.dino.position(), 12.0, 0.9F, 0.3F);
        float amount = this.damage(1.0F);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.dino.getBoundingBox().inflate(TAIL_RADIUS, 2.0, TAIL_RADIUS), this::hittable)) {
            if (this.facing(victim) > 0.45 || victim.distanceTo(this.dino) > TAIL_RADIUS + 0.5) {
                continue;
            }
            Vec3 off = new Vec3(victim.getX() - this.dino.getX(), 0.0, victim.getZ() - this.dino.getZ());
            Vec3 out = off.lengthSqr() < 1.0E-4 ? this.forward().scale(-1.0) : off.normalize();
            Vec3 swing = new Vec3(-this.forward().z, 0.0, this.forward().x);
            Vec3 push = out.scale(0.9).add(swing.scale(0.7));
            this.strike(level, victim, amount, new Vec3(push.x, 0.45, push.z));
        }
    }

    private void roar(ServerLevel level) {
        AbyssaurFx.roar(level, this.dino, ROAR_RADIUS);
        NightbladeShakeMessage.around(level, this.dino.position(), ROAR_RADIUS + 6.0, 1.5F, 1.0F);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.dino.getBoundingBox().inflate(ROAR_RADIUS), this::hittable)) {
            if (victim.distanceTo(this.dino) > ROAR_RADIUS) {
                continue;
            }
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
            victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
            AbyssaurFx.dread(level, victim);
        }
    }

    private void breath(ServerLevel level) {
        int t = this.attackTick - Attack.BREATH.impact;
        Vec3 dir = this.forward().yRot((float)(Math.sin(t * 0.35) * 0.25));
        Vec3 jaws = this.jaws();
        AbyssaurFx.breath(level, jaws, dir, BREATH_RANGE, BREATH_HALF_ANGLE);
        if (t % 4 == 0) {
            float amount = this.damage(2.0F / 15.0F);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(jaws, jaws).inflate(BREATH_RANGE, 3.0, BREATH_RANGE), this::hittable)) {
                Vec3 to = victim.getBoundingBox().getCenter().subtract(jaws);
                Vec3 flat = new Vec3(to.x, 0.0, to.z);
                if (flat.length() <= BREATH_RANGE && flat.length() > 0.01 && flat.normalize().dot(dir) >= Math.cos(BREATH_HALF_ANGLE)) {
                    victim.hurtServer(level, this.dino.damageSources().mobAttack(this.dino), amount);
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                }
            }
        }
        if (t == 3 || t == 8 || t == 13) {
            double d = 3.5 + (t / 13.0) * 4.0;
            Vec3 at = this.dino.position().add(dir.scale(d + 1.5));
            Double ground = this.groundNear(level, at);
            if (ground != null) {
                this.pools.add(new Pool(new Vec3(at.x, ground, at.z), level.getGameTime() + 140));
            }
        }
    }

    private Double groundNear(ServerLevel level, Vec3 at) {
        net.minecraft.core.BlockPos base = net.minecraft.core.BlockPos.containing(at.x, at.y - 0.5, at.z);
        for (int dy = 2; dy >= -3; dy--) {
            net.minecraft.core.BlockPos pos = base.above(dy);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return (double)pos.getY() + 1.0;
            }
        }
        return null;
    }

    private void tickPools(ServerLevel level) {
        if (this.pools.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        this.pools.removeIf(p -> p.until() <= now);
        for (Pool p : this.pools) {
            AbyssaurFx.pool(level, p.at(), POOL_RADIUS, (int)(now % 1000));
            if (now % 20 == 0) {
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(p.at(), p.at()).inflate(POOL_RADIUS, 1.0, POOL_RADIUS), this::hittable)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                }
            }
        }
    }
}
