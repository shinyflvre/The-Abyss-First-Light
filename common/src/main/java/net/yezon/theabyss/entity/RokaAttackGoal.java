package net.yezon.theabyss.entity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
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

public class RokaAttackGoal extends Goal {
    enum Attack {
        COMBO("combo", 7, 27),
        SMASH("smash", 18, 30),
        ROAR("roar", 10, 32),
        CHARGE("charge_windup", 24, 24 + 34),
        CHARGE_END("charge_stop", -1, 16),
        LEAP("leap_crouch", 10, 10 + 40),
        LEAP_LAND("leap_land", -1, 20),
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

    private static final int UPPERCUT_TICK = 18;
    private static final double SMASH_RANGE = 6.5;
    private static final double SMASH_HALF_ANGLE = Math.toRadians(35.0);
    private static final double ROAR_RADIUS = 10.0;
    private static final double RUN_SPEED = 0.7;
    private static final double RUN_TURN = Math.toRadians(3.5);
    private static final int RUN_TICKS = 34;
    private static final float LEAP_RADIUS = 4.5F;

    private final TheRokaEntity roka;
    private Attack attack;
    private int attackTick;
    private int cooldown;
    private int smashCooldown = 40;
    private int roarCooldown = 120;
    private int chargeCooldown = 60;
    private int leapCooldown = 80;
    private int repath;
    private LivingEntity attackTarget;
    private long enragedUntil;
    private Vec3 runDir = Vec3.ZERO;
    private boolean cueSent;
    private final Set<Integer> runHit = new HashSet<>();
    private Vec3 leapFrom = Vec3.ZERO;
    private Vec3 leapTo = Vec3.ZERO;
    private int leapFlight;
    boolean devHold;

    public RokaAttackGoal(TheRokaEntity roka) {
        this.roka = roka;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.roka.getTarget();
        return target != null && target.isAlive();
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
        if (this.attack == Attack.LEAP) {
            this.roka.setNoGravity(false);
        }
        this.attack = null;
        this.roka.getNavigation().stop();
    }

    boolean enraged() {
        return this.roka.level().getGameTime() < this.enragedUntil;
    }

    private float damage(float factor) {
        float base = (float)this.roka.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
        return TheRokaEntity.halfHearts(this.enraged() ? base * 1.25F : base);
    }

    @Override
    public void tick() {
        if (!(this.roka.level() instanceof ServerLevel level)) {
            return;
        }
        this.cooldown--;
        this.smashCooldown--;
        this.roarCooldown--;
        this.chargeCooldown--;
        this.leapCooldown--;
        if (this.enraged()) {
            RokaFx.rage(level, this.roka, this.roka.tickCount);
        }
        if (this.attack != null) {
            this.tickAttack(level);
            return;
        }
        LivingEntity target = this.roka.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.roka.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.devHold) {
            this.roka.getNavigation().stop();
            return;
        }
        if (--this.repath <= 0 || this.roka.getNavigation().isDone()) {
            this.repath = 6 + this.roka.getRandom().nextInt(6);
            this.roka.getNavigation().moveTo(target, this.enraged() ? 1.35 : 1.15);
        }
        if (this.cooldown <= 0 && this.roka.getSensing().hasLineOfSight(target)) {
            Attack next = this.choose(level, target);
            if (next != null) {
                this.begin(next, target);
            }
        }
    }

    private double reachTo(LivingEntity target) {
        double dx = target.getX() - this.roka.getX();
        double dz = target.getZ() - this.roka.getZ();
        return Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
    }

    private Attack choose(ServerLevel level, LivingEntity target) {
        double reach = this.reachTo(target);
        double dy = Math.abs(target.getY() - this.roka.getY());
        int[] w = new int[Attack.values().length];
        if (dy <= 3.0) {
            w[Attack.COMBO.ordinal()] = reach <= 2.8 ? 50 : 0;
            w[Attack.SMASH.ordinal()] = reach <= SMASH_RANGE - 1.0 && this.smashCooldown <= 0 ? (reach <= 2.8 ? 25 : 40) : 0;
        }
        if (reach <= ROAR_RADIUS && this.roarCooldown <= 0 && !this.enraged()) {
            int players = level.getEntitiesOfClass(Player.class, this.roka.getBoundingBox().inflate(ROAR_RADIUS), this::hittable).size();
            w[Attack.ROAR.ordinal()] = 18 + 30 * Math.max(0, players - 1) + (this.roka.getHealth() < this.roka.getMaxHealth() * 0.5F ? 25 : 0);
        }
        if (dy <= 2.0 && reach >= 6.0 && reach <= 22.0 && this.chargeCooldown <= 0) {
            w[Attack.CHARGE.ordinal()] = 35;
        }
        if (reach >= 5.0 && reach <= 18.0 && dy <= 9.0 && this.leapCooldown <= 0) {
            w[Attack.LEAP.ordinal()] = dy > 2.5 ? 70 : 30;
        }
        int total = 0;
        for (int v : w) {
            total += v;
        }
        if (total <= 0) {
            return null;
        }
        int roll = this.roka.getRandom().nextInt(total);
        for (Attack a : Attack.values()) {
            roll -= w[a.ordinal()];
            if (roll < 0) {
                return a;
            }
        }
        return null;
    }

    void devStop() {
        this.roka.setNoGravity(false);
        this.attack = null;
        this.roka.getNavigation().stop();
        this.roka.setDeltaMovement(Vec3.ZERO);
    }

    void devForce(String name, LivingEntity target) {
        for (Attack a : Attack.values()) {
            if (a.impact >= 0 && (a.animation.equals(name) || a.animation.startsWith(name + "_"))) {
                this.roka.setTarget(target);
                this.begin(a, target);
                return;
            }
        }
    }

    private void begin(Attack next, LivingEntity target) {
        ServerLevel level = (ServerLevel)this.roka.level();
        this.attack = next;
        this.attackTick = 0;
        this.attackTarget = target;
        this.roka.getNavigation().stop();
        this.roka.triggerAnim("action", next.animation);
        switch (next) {
            case SMASH -> this.smashCooldown = 120 + this.roka.getRandom().nextInt(60);
            case ROAR -> this.roarCooldown = 400 + this.roka.getRandom().nextInt(200);
            case CHARGE -> {
                this.chargeCooldown = 200 + this.roka.getRandom().nextInt(80);
                this.runHit.clear();
                this.cueSent = false;
            }
            case LEAP -> this.leapCooldown = 180 + this.roka.getRandom().nextInt(80);
            default -> {
            }
        }
    }

    private void startAnimation(Attack next) {
        this.attack = next;
        this.attackTick = 0;
        this.roka.triggerAnim("action", next.animation);
    }

    private void tickAttack(ServerLevel level) {
        Attack current = this.attack;
        this.attackTick++;
        LivingEntity target = this.attackTarget;
        boolean aim = target != null && target.isAlive();
        switch (current) {
            case COMBO -> {
                if (aim && this.attackTick < UPPERCUT_TICK - 2) {
                    this.face(target.position().subtract(this.roka.position()));
                }
                if (aim && this.attackTick > current.impact && this.attackTick < UPPERCUT_TICK - 1 && this.reachTo(target) > 1.6) {
                    Vec3 step = new Vec3(target.getX() - this.roka.getX(), 0.0, target.getZ() - this.roka.getZ()).normalize().scale(0.22);
                    this.roka.setDeltaMovement(step.x, this.roka.getDeltaMovement().y, step.z);
                }
                if (this.attackTick == current.impact) {
                    this.hook(level, target);
                } else if (this.attackTick == UPPERCUT_TICK) {
                    this.uppercut(level, target);
                }
            }
            case SMASH -> {
                if (aim && this.attackTick < current.impact - 4) {
                    this.face(target.position().subtract(this.roka.position()));
                }
                if (this.attackTick == current.impact) {
                    this.smash(level);
                }
            }
            case ROAR -> {
                if (aim) {
                    this.face(target.position().subtract(this.roka.position()));
                }
                if (this.attackTick == current.impact) {
                    this.roar(level);
                }
            }
            case CHARGE -> {
                this.tickCharge(level, target);
                return;
            }
            case LEAP -> {
                this.tickLeap(level, target);
                return;
            }
            default -> {
            }
        }
        this.roka.getNavigation().stop();
        if (this.attackTick >= current.length) {
            this.attack = null;
            this.cooldown = 8 + this.roka.getRandom().nextInt(10);
        }
    }

    Vec3 forward() {
        float yaw = this.roka.getYRot() * ((float)Math.PI / 180.0F);
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private Vec3 left() {
        Vec3 f = this.forward();
        return new Vec3(f.z, 0.0, -f.x);
    }

    private void face(Vec3 dir) {
        if (dir.horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        float yaw = (float)(Math.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        this.roka.setYRot(yaw);
        this.roka.setYBodyRot(yaw);
        this.roka.setYHeadRot(yaw);
    }

    private boolean inFront(LivingEntity target, double reach, double cone) {
        if (target == null || !target.isAlive() || this.reachTo(target) > reach || Math.abs(target.getY() - this.roka.getY()) > 3.0) {
            return false;
        }
        Vec3 to = target.position().subtract(this.roka.position());
        Vec3 flat = new Vec3(to.x, 0.0, to.z);
        return flat.lengthSqr() < 0.01 || flat.normalize().dot(this.forward()) >= cone;
    }

    private boolean strike(ServerLevel level, LivingEntity victim, float amount, Vec3 velocity) {
        if (!victim.hurtServer(level, this.roka.damageSources().mobAttack(this.roka), amount)) {
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

    private Vec3 away(LivingEntity victim, double horizontal, double up) {
        Vec3 off = victim.position().subtract(this.roka.position());
        Vec3 dir = new Vec3(off.x, 0.0, off.z).lengthSqr() < 1.0E-4 ? this.forward() : new Vec3(off.x, 0.0, off.z).normalize();
        return new Vec3(dir.x * horizontal, up, dir.z * horizontal);
    }

    boolean hittable(LivingEntity e) {
        if (e == this.roka || !e.isAlive()) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(e instanceof Monster) || e == this.roka.getTarget();
    }

    private void hook(ServerLevel level, LivingEntity target) {
        Vec3 fist = this.roka.position().add(this.forward().scale(1.3)).add(this.left().scale(0.4)).add(0.0, 1.9, 0.0);
        boolean hit = this.inFront(target, 3.2, 0.2) && this.strike(level, target, this.damage(0.5F), null);
        RokaFx.hook(level, fist, hit);
    }

    private void uppercut(ServerLevel level, LivingEntity target) {
        Vec3 fist = this.roka.position().add(this.forward().scale(1.2)).add(this.left().scale(-0.4)).add(0.0, 2.2, 0.0);
        boolean hit = this.inFront(target, 3.6, 0.1) && this.strike(level, target, this.damage(1.0F), this.away(target, 0.35, 0.95));
        RokaFx.uppercut(level, fist, hit);
    }

    private void smash(ServerLevel level) {
        Vec3 centre = this.roka.position().add(this.forward().scale(1.4));
        RokaFx.smash(level, centre, this.forward(), SMASH_RANGE, SMASH_HALF_ANGLE);
        NightbladeShakeMessage.around(level, centre, 16.0, 1.1F, 0.4F);
        float amount = this.damage(1.0F);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(SMASH_RANGE + 1.0, 3.0, SMASH_RANGE + 1.0), this::hittable)) {
            Vec3 off = victim.position().subtract(this.roka.position());
            Vec3 flat = new Vec3(off.x, 0.0, off.z);
            double distance = flat.length();
            boolean inCone = distance < 1.5 || flat.normalize().dot(this.forward()) >= Math.cos(SMASH_HALF_ANGLE);
            if (distance <= SMASH_RANGE + 1.4 && inCone && Math.abs(victim.getY() - this.roka.getY()) < 2.5) {
                if (this.strike(level, victim, amount, this.away(victim, 1.1, 0.45))) {
                    RokaFx.hitSpark(level, victim);
                }
            }
        }
    }

    private void roar(ServerLevel level) {
        RokaFx.roar(level, this.roka);
        NightbladeShakeMessage.around(level, this.roka.position(), ROAR_RADIUS + 4.0, 1.4F, 0.8F);
        this.enragedUntil = level.getGameTime() + 200;
        this.roka.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, false));
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.roka.getBoundingBox().inflate(ROAR_RADIUS), this::hittable)) {
            if (victim.distanceTo(this.roka) > ROAR_RADIUS) {
                continue;
            }
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            Vec3 push = this.away(victim, 1.0, 0.3);
            victim.setDeltaMovement(push);
            victim.needsSync = true;
            victim.syncVelocity = true;
        }
    }

    private void tickCharge(ServerLevel level, LivingEntity target) {
        int run = this.attackTick - Attack.CHARGE.impact;
        this.roka.getNavigation().stop();
        if (run < 0) {
            if (target != null && target.isAlive()) {
                this.face(target.position().subtract(this.roka.position()));
            }
            if (this.attackTick % 8 == 6) {
                RokaFx.scrape(level, this.roka);
            }
            return;
        }
        if (run == 0) {
            this.roka.triggerAnim("action", "charge_run");
            this.runDir = this.forward();
        }
        if (target != null && target.isAlive()) {
            Vec3 to = target.position().subtract(this.roka.position());
            Vec3 flat = new Vec3(to.x, 0.0, to.z);
            if (flat.lengthSqr() > 1.0E-4) {
                double want = Math.atan2(flat.z, flat.x);
                double have = Math.atan2(this.runDir.z, this.runDir.x);
                double turn = Math.atan2(Math.sin(want - have), Math.cos(want - have));
                double behind = flat.normalize().dot(this.runDir);
                if (behind > 0.0) {
                    have += Math.max(-RUN_TURN, Math.min(RUN_TURN, turn));
                }
                this.runDir = new Vec3(Math.cos(have), 0.0, Math.sin(have));
            }
            if (target instanceof ServerPlayer player && flat.length() < 12.0 && (!this.cueSent || run % 3 == 0)) {
                double contact = this.roka.getBbWidth() * 0.5 + 0.6 + 0.7 + target.getBbWidth() * 0.5;
                int ticks = (int)Math.ceil(Math.max(0.0, flat.length() - contact) / RUN_SPEED);
                NightbladeParryCueMessage.send(player, this.roka.getId(), ticks, NightbladeParry.MINI_BOSS_WINDOW);
                this.cueSent = true;
            }
        }
        this.face(this.runDir);
        if (run > 2 && this.roka.horizontalCollision && this.wallAhead(level)) {
            RokaFx.wall(level, this.roka.position().add(this.runDir.scale(0.8)).add(0.0, 1.6, 0.0));
            NightbladeShakeMessage.around(level, this.roka.position(), 12.0, 1.0F, 0.4F);
            this.roka.setDeltaMovement(this.runDir.scale(-0.3).add(0.0, 0.2, 0.0));
            this.startAnimation(Attack.STAGGER);
            this.attack = Attack.STAGGER;
            this.attackTick = -20;
            return;
        }
        if (run >= RUN_TICKS) {
            this.endCharge(false);
            return;
        }
        this.roka.setDeltaMovement(this.runDir.x * RUN_SPEED, this.roka.getDeltaMovement().y, this.runDir.z * RUN_SPEED);
        if (run % 2 == 0) {
            RokaFx.chargeStep(level, this.roka);
        }
        AABB horns = this.roka.getBoundingBox().inflate(0.6, 0.0, 0.6).move(this.runDir.scale(0.7));
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, horns, this::hittable)) {
            if (!this.runHit.add(victim.getId())) {
                continue;
            }
            if (victim instanceof ServerPlayer player && NightbladeParry.consume(player, NightbladeParry.MINI_BOSS_WINDOW)) {
                Vec3 at = victim.position().add(0.0, 1.2, 0.0);
                NightbladeParry.playParryEffect(level, at);
                NightbladeImpactMessage.send(player, true);
                this.roka.setDeltaMovement(this.runDir.scale(0.4));
                this.startAnimation(Attack.STAGGER);
                return;
            }
            if (this.strike(level, victim, this.damage(1.0F), this.runDir.scale(0.6).add(0.0, 1.25, 0.0))) {
                RokaFx.gore(level, victim);
                NightbladeShakeMessage.around(level, victim.position(), 10.0, 0.9F, 0.35F);
                this.endCharge(true);
                return;
            }
        }
    }

    private boolean wallAhead(ServerLevel level) {
        Vec3 probe = this.roka.position().add(this.runDir.scale(this.roka.getBbWidth() * 0.5 + 0.6));
        BlockPos pos = BlockPos.containing(probe.x, this.roka.getY() + 1.2, probe.z);
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
            && !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private void endCharge(boolean gored) {
        this.roka.setDeltaMovement(this.roka.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        this.attack = Attack.CHARGE_END;
        this.attackTick = 0;
        this.roka.triggerAnim("action", gored ? "charge_gore" : "charge_stop");
    }

    private void tickLeap(ServerLevel level, LivingEntity target) {
        int t = this.attackTick - Attack.LEAP.impact;
        this.roka.getNavigation().stop();
        if (t < 0) {
            if (target != null && target.isAlive()) {
                this.face(target.position().subtract(this.roka.position()));
            }
            return;
        }
        if (t == 0) {
            Vec3 aim = target != null && target.isAlive() ? target.position().add(target.getDeltaMovement().multiply(6.0, 0.0, 6.0)) : this.roka.position().add(this.forward().scale(8.0));
            this.leapFrom = this.roka.position();
            this.leapTo = aim;
            this.leapFlight = (int)Math.max(14, Math.min(26, Math.round(this.leapFrom.distanceTo(aim) * 1.3)));
            this.roka.setNoGravity(true);
            this.roka.triggerAnim("action", "leap_air");
            RokaFx.leap(level, this.leapFrom, this.leapTo, LEAP_RADIUS, this.leapFlight);
        }
        if (t >= this.leapFlight || t > 3 && this.roka.onGround() || t > 3 && this.roka.horizontalCollision) {
            this.land(level);
            return;
        }
        double s0 = t / (double)this.leapFlight;
        double s1 = (t + 1) / (double)this.leapFlight;
        Vec3 next = this.arc(s1);
        Vec3 now = this.arc(s0);
        this.roka.setDeltaMovement(next.subtract(now));
        this.face(this.leapTo.subtract(this.leapFrom));
    }

    private Vec3 arc(double s) {
        Vec3 flat = this.leapFrom.lerp(this.leapTo, s);
        double peak = Math.max(this.leapFrom.y, this.leapTo.y) + 5.0;
        double y0 = this.leapFrom.y;
        double y1 = this.leapTo.y;
        double y = y0 * (1 - s) * (1 - 2 * s) + peak * 4 * s * (1 - s) + y1 * s * (2 * s - 1);
        return new Vec3(flat.x, y, flat.z);
    }

    private void land(ServerLevel level) {
        this.roka.setNoGravity(false);
        this.roka.setDeltaMovement(Vec3.ZERO);
        this.roka.resetFallDistance();
        Vec3 at = this.roka.position();
        RokaFx.land(level, at, LEAP_RADIUS);
        NightbladeShakeMessage.around(level, at, 18.0, 1.4F, 0.5F);
        float amount = this.damage(1.0F);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.roka.getBoundingBox().inflate(LEAP_RADIUS, 2.0, LEAP_RADIUS), this::hittable)) {
            double dx = victim.getX() - at.x;
            double dz = victim.getZ() - at.z;
            if (dx * dx + dz * dz <= LEAP_RADIUS * LEAP_RADIUS && victim.getY() - at.y < 2.0) {
                if (this.strike(level, victim, amount, this.away(victim, 0.8, 0.6))) {
                    RokaFx.hitSpark(level, victim);
                }
            }
        }
        this.startAnimation(Attack.LEAP_LAND);
    }
}
