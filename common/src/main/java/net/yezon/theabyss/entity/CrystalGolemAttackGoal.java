package net.yezon.theabyss.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.network.NightbladeParryCueMessage;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.network.NightbladeShakeMessage;

public class CrystalGolemAttackGoal extends Goal {
    enum Attack {
        PUNCH("punch", 7, 16),
        IRON_PUNCH("iron_punch", 20, 32),
        SLAM("slam", 22, 40),
        ERUPTION("eruption", 13, 30),
        THROW("throw", 18, 28),
        CHARGE("charge_windup", 16, 16 + RUN_TICKS),
        CHARGE_STOP("charge_stop", -1, 16),
        STAGGER("stagger", -1, 30),
        DAZED("charge_stunned", -1, 60);

        final String animation;
        final int impact;
        final int length;

        Attack(String animation, int impact, int length) {
            this.animation = animation;
            this.impact = impact;
            this.length = length;
        }
    }

    static final float SLAM_RADIUS = 8.0F;
    static final int RUN_TICKS = 26;
    private static final double RUN_SPEED = 0.6;
    private static final double CHARGE_LENGTH = 14.0;
    private static final double ERUPTION_RANGE = 15.0;
    private static final double ERUPTION_STEP = 1.15;
    private static final double CRACK_SPEED = 1.1;
    private static final int SPIKE_WARMUP = 8;
    private static final float WAVE_SPEED = 0.75F;
    private static final double KNOCKBACK_SPEED = 2.5;
    private static final double KNOCKBACK_LIFT = 0.6;

    private final CrystalGolemEntity golem;
    private Attack attack;
    private int attackTick;
    private int cooldown;
    private int ironCooldown = 60;
    private int slamCooldown = 100;
    private int eruptionCooldown = 40;
    private int throwCooldown = 60;
    private int chargeCooldown = 80;
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 chargeFrom = Vec3.ZERO;
    private final Set<Integer> chargeHit = new HashSet<>();

    private record PendingSpike(Vec3 at, float yaw, long time) {
    }

    private final List<PendingSpike> pendingSpikes = new ArrayList<>();
    private int repath;
    private LivingEntity attackTarget;
    private Vec3 waveCentre;
    private int waveTick = -1;
    private final Set<Integer> waveHit = new HashSet<>();
    boolean devHold;

    public CrystalGolemAttackGoal(CrystalGolemEntity golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.golem.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.attack != null || this.waveTick >= 0 || !this.pendingSpikes.isEmpty() || this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.golem.setIronFist(false);
        this.attack = null;
        this.golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!(this.golem.level() instanceof ServerLevel level)) {
            return;
        }
        this.cooldown--;
        this.ironCooldown--;
        this.slamCooldown--;
        this.eruptionCooldown--;
        this.throwCooldown--;
        this.chargeCooldown--;
        this.spawnPendingSpikes(level);
        if (this.waveTick >= 0) {
            this.tickWave(level);
        }
        if (this.attack != null) {
            this.tickAttack(level);
            return;
        }
        LivingEntity target = this.golem.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.devHold) {
            this.golem.getNavigation().stop();
            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }
        this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--this.repath <= 0 || this.golem.getNavigation().isDone()) {
            this.repath = 8 + this.golem.getRandom().nextInt(6);
            this.golem.getNavigation().moveTo(target, 1.2);
        }
        double reach = this.reachTo(target);
        if (this.cooldown <= 0 && this.golem.getSensing().hasLineOfSight(target)) {
            Attack next = this.choose(level, target, reach);
            if (next != null) {
                this.begin(next, target);
            }
        }
    }

    private double reachTo(LivingEntity target) {
        double dx = target.getX() - this.golem.getX();
        double dz = target.getZ() - this.golem.getZ();
        return Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
    }

    private Attack choose(ServerLevel level, LivingEntity target, double reach) {
        double dy = Math.abs(target.getY() - this.golem.getY());
        int[] weights = new int[Attack.values().length];
        if (dy <= 3.0) {
            weights[Attack.PUNCH.ordinal()] = reach <= 2.6 ? 50 : 0;
            weights[Attack.IRON_PUNCH.ordinal()] = reach <= 3.0 && this.ironCooldown <= 0 ? 35 : 0;
            if (reach <= 4.5 && this.slamCooldown <= 0 && target.onGround()) {
                weights[Attack.SLAM.ordinal()] = 30 + 40 * Math.max(0, level.getEntitiesOfClass(Player.class, this.golem.getBoundingBox().inflate(6.0), this::hittable).size() - 1);
            }
            if (reach > 4.5 && reach <= ERUPTION_RANGE - 2.0 && this.eruptionCooldown <= 0) {
                weights[Attack.ERUPTION.ordinal()] = 35;
            }
        }
        if (dy <= 1.5 && reach >= 6.0 && reach <= CHARGE_LENGTH && this.chargeCooldown <= 0) {
            weights[Attack.CHARGE.ordinal()] = 30;
        }
        if (reach >= 6.0 && reach <= 26.0 && this.throwCooldown <= 0) {
            Path path = this.golem.getNavigation().getPath();
            boolean unreachable = dy > 3.0 || path != null && path.isDone() && !path.canReach();
            weights[Attack.THROW.ordinal()] = unreachable ? 90 : 25;
        }
        int total = 0;
        for (int w : weights) {
            total += w;
        }
        if (total <= 0) {
            return null;
        }
        int roll = this.golem.getRandom().nextInt(total);
        for (Attack a : Attack.values()) {
            roll -= weights[a.ordinal()];
            if (roll < 0) {
                return a;
            }
        }
        return null;
    }

    void devForce(String name, LivingEntity target) {
        for (Attack a : Attack.values()) {
            if (a.animation.equals(name) && a.impact >= 0 || a == Attack.CHARGE && name.equals("charge")) {
                this.golem.setTarget(target);
                this.begin(a, target);
            }
        }
    }

    private void begin(Attack next, LivingEntity target) {
        this.attack = next;
        this.attackTick = 0;
        this.attackTarget = target;
        this.golem.getNavigation().stop();
        this.golem.triggerAnim("action", next.animation);
        ServerLevel level = (ServerLevel)this.golem.level();
        if (next == Attack.IRON_PUNCH) {
            this.ironCooldown = 140 + this.golem.getRandom().nextInt(60);
            this.golem.setIronFist(true);
            CrystalGolemFx.ironCharge(level, this.golem);
            if (target instanceof ServerPlayer player) {
                NightbladeParryCueMessage.send(player, this.golem.getId(), next.impact, NightbladeParry.MINI_BOSS_WINDOW);
            }
        } else if (next == Attack.SLAM) {
            this.slamCooldown = 200 + this.golem.getRandom().nextInt(80);
            CrystalGolemFx.slamRaise(level, this.golem);
        } else if (next == Attack.ERUPTION) {
            this.eruptionCooldown = 160 + this.golem.getRandom().nextInt(60);
        } else if (next == Attack.THROW) {
            this.throwCooldown = 140 + this.golem.getRandom().nextInt(60);
        } else if (next == Attack.CHARGE) {
            this.chargeCooldown = 220 + this.golem.getRandom().nextInt(80);
            Vec3 aim = target.position().add(target.getDeltaMovement().multiply(8.0, 0.0, 8.0)).subtract(this.golem.position());
            this.chargeDir = new Vec3(aim.x, 0.0, aim.z).lengthSqr() < 1.0E-4 ? this.forward() : new Vec3(aim.x, 0.0, aim.z).normalize();
            this.chargeHit.clear();
            this.face(this.chargeDir);
            CrystalGolemFx.chargeWindup(level, this.golem.position(), this.chargeDir, CHARGE_LENGTH + 1.0);
        }
    }

    void daze(int ticks) {
        this.attack = Attack.DAZED;
        this.attackTick = Attack.DAZED.length - ticks;
        this.golem.setIronFist(false);
        this.golem.getNavigation().stop();
        this.golem.triggerAnim("action", Attack.DAZED.animation);
    }

    boolean dazed() {
        return this.attack == Attack.DAZED;
    }

    private void face(Vec3 dir) {
        float yaw = (float)(Math.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        this.golem.setYRot(yaw);
        this.golem.setYBodyRot(yaw);
        this.golem.setYHeadRot(yaw);
    }

    private void tickAttack(ServerLevel level) {
        Attack current = this.attack;
        this.attackTick++;
        LivingEntity target = this.attackTarget;
        if (current.impact >= 0 && current != Attack.CHARGE && target != null && target.isAlive() && this.attackTick < current.impact - 3) {
            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Vec3 to = target.position().subtract(this.golem.position());
            float yaw = (float)(Math.atan2(to.z, to.x) * (180.0 / Math.PI)) - 90.0F;
            this.golem.setYRot(yaw);
            this.golem.setYBodyRot(yaw);
            this.golem.setYHeadRot(yaw);
        }
        this.golem.getNavigation().stop();
        if (current == Attack.CHARGE) {
            this.tickCharge(level);
            return;
        }
        if (current == Attack.DAZED) {
            CrystalGolemFx.dazedTick(level, this.golem, this.attackTick);
        }
        if (current == Attack.THROW && this.attackTick == 6) {
            CrystalGolemFx.throwGrab(level, this.golem.position().add(this.left().scale(0.9)).add(0.0, 3.0, 0.0));
        }
        if (current == Attack.SLAM && this.attackTick == 8) {
            CrystalGolemFx.slamTelegraph(level, this.slamCentre(), SLAM_RADIUS);
        }
        if (this.attackTick == current.impact) {
            switch (current) {
                case PUNCH -> this.punch(level, target);
                case IRON_PUNCH -> this.ironPunch(level, target);
                case SLAM -> this.slam(level);
                case ERUPTION -> this.eruption(level, target);
                case THROW -> this.throwCrystal(level, target);
                default -> {
                }
            }
        }
        if (current == Attack.IRON_PUNCH && this.attackTick == current.impact + 4) {
            this.golem.setIronFist(false);
        }
        if (this.attackTick >= current.length) {
            this.attack = null;
            this.golem.setIronFist(false);
            this.cooldown = 10 + this.golem.getRandom().nextInt(12);
        }
    }

    private boolean inFront(LivingEntity target, double reach, double cone) {
        if (target == null || !target.isAlive() || this.reachTo(target) > reach || Math.abs(target.getY() - this.golem.getY()) > 2.5) {
            return false;
        }
        Vec3 to = target.position().subtract(this.golem.position());
        Vec3 flat = new Vec3(to.x, 0.0, to.z);
        if (flat.lengthSqr() < 0.01) {
            return true;
        }
        return flat.normalize().dot(this.forward()) >= cone;
    }

    Vec3 forward() {
        float yaw = this.golem.getYRot() * ((float)Math.PI / 180.0F);
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private Vec3 left() {
        Vec3 f = this.forward();
        return new Vec3(f.z, 0.0, -f.x);
    }

    private void punch(ServerLevel level, LivingEntity target) {
        Vec3 fist = this.golem.position().add(this.forward().scale(1.2)).add(this.left().scale(-0.2)).add(0.0, 1.8, 0.0);
        level.playSound(null, fist.x, fist.y, fist.z, SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.0F, 0.75F);
        if (this.inFront(target, 3.0, 0.2)) {
            this.golem.doHurtTarget(target);
            CrystalGolemFx.punchHit(level, fist);
        }
    }

    private void ironPunch(ServerLevel level, LivingEntity target) {
        Vec3 fist = this.golem.position().add(this.forward().scale(1.55)).add(this.left().scale(0.4)).add(0.0, 1.55, 0.0);
        if (!this.inFront(target, 3.6, 0.1)) {
            CrystalGolemFx.ironMiss(level, fist);
            return;
        }
        if (target instanceof ServerPlayer player && NightbladeParry.consume(player, NightbladeParry.MINI_BOSS_WINDOW)) {
            NightbladeParry.playParryEffect(level, fist);
            NightbladeImpactMessage.send(player, true);
            CrystalGolemFx.ironParried(level, fist);
            this.golem.setIronFist(false);
            this.golem.knockback(0.6, this.forward().x, this.forward().z);
            this.golem.hasImpulse = true;
            this.attack = Attack.STAGGER;
            this.attackTick = 0;
            this.golem.triggerAnim("action", Attack.STAGGER.animation);
            return;
        }
        float damage = (float)this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (target.hurt(this.golem.damageSources().mobAttack(this.golem), damage)) {
            Vec3 away = target.position().subtract(this.golem.position());
            Vec3 dir = new Vec3(away.x, 0.0, away.z).lengthSqr() < 1.0E-4 ? this.forward() : new Vec3(away.x, 0.0, away.z).normalize();
            target.setDeltaMovement(dir.x * KNOCKBACK_SPEED, KNOCKBACK_LIFT, dir.z * KNOCKBACK_SPEED);
            target.resetFallDistance();
            target.hasImpulse = true;
            target.hurtMarked = true;
            NightbladeShakeMessage.around(level, fist, 10.0, 0.9F, 0.35F);
        }
        CrystalGolemFx.ironHit(level, fist, this.forward());
    }

    private Vec3 slamCentre() {
        return this.golem.position().add(this.forward().scale(1.0));
    }

    private void slam(ServerLevel level) {
        this.waveCentre = this.slamCentre();
        this.waveTick = 0;
        this.waveHit.clear();
        CrystalGolemFx.slamImpact(level, this.waveCentre, SLAM_RADIUS);
        NightbladeShakeMessage.around(level, this.waveCentre, 18.0, 1.3F, 0.5F);
        this.tickWave(level);
    }

    private void eruption(ServerLevel level, LivingEntity target) {
        Vec3 foot = this.golem.position().add(this.forward().scale(0.6)).add(this.left().scale(-0.5));
        CrystalGolemFx.eruptionStomp(level, foot);
        NightbladeShakeMessage.around(level, foot, 14.0, 0.8F, 0.3F);
        Vec3 aim = target != null ? target.position().add(target.getDeltaMovement().multiply(6.0, 0.0, 6.0)).subtract(this.golem.position()) : this.forward();
        Vec3 flat = new Vec3(aim.x, 0.0, aim.z).lengthSqr() < 1.0E-4 ? this.forward() : new Vec3(aim.x, 0.0, aim.z).normalize();
        long now = level.getGameTime();
        for (double spread : new double[] {0.0, -0.3, 0.3}) {
            Vec3 dir = flat.yRot((float)spread);
            float yaw = (float)Math.toDegrees(Math.atan2(-dir.x, dir.z));
            double y = this.golem.getY();
            for (double d = 2.0; d <= ERUPTION_RANGE; d += ERUPTION_STEP) {
                Vec3 at = this.golem.position().add(dir.scale(d));
                Double ground = this.groundNear(level, at.x, y, at.z);
                if (ground == null) {
                    break;
                }
                y = ground;
                this.pendingSpikes.add(new PendingSpike(new Vec3(at.x, ground, at.z), yaw, now + (long)Math.ceil(d / CRACK_SPEED)));
            }
        }
    }

    private Double groundNear(ServerLevel level, double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y - 0.5, z);
        for (int dy = 2; dy >= -3; dy--) {
            BlockPos pos = base.above(dy);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty()) {
                return (double)pos.getY() + 1.0;
            }
        }
        return null;
    }

    private void spawnPendingSpikes(ServerLevel level) {
        if (this.pendingSpikes.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        float damage = (float)this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        this.pendingSpikes.removeIf(spike -> {
            if (spike.time() > now) {
                return false;
            }
            NightbladeSpikeEntity entity = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level);
            if (entity != null) {
                entity.setPos(spike.at());
                entity.setupGolem(this.golem, SPIKE_WARMUP, damage, spike.yaw());
                level.addFreshEntity(entity);
            }
            return true;
        });
    }

    private void throwCrystal(ServerLevel level, LivingEntity target) {
        Vec3 hand = this.golem.position().add(this.forward().scale(0.6)).add(this.left().scale(-0.6)).add(0.0, 2.7, 0.0);
        CrystalGolemFx.throwRelease(level, hand);
        if (target == null || !target.isAlive()) {
            return;
        }
        double distance = target.position().distanceTo(this.golem.position());
        int flight = (int)Math.max(12, Math.min(28, Math.round(distance * 1.1)));
        Vec3 aim = target.position().add(0.0, 0.4, 0.0).add(target.getDeltaMovement().multiply(flight * 0.6, 0.0, flight * 0.6));
        Vec3 velocity = aim.subtract(hand).add(0.0, CrystalGolemShardEntity.GRAVITY * flight * (flight + 1) / 2.0, 0.0).scale(1.0 / flight);
        CrystalGolemShardEntity shard = TheabyssModEntities.CRYSTAL_GOLEM_SHARD.get().create(level);
        if (shard == null) {
            return;
        }
        shard.setPos(hand);
        shard.launch(this.golem, velocity, (float)this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
        level.addFreshEntity(shard);
        if (target instanceof ServerPlayer player) {
            NightbladeParryCueMessage.send(player, shard.getId(), flight, NightbladeParry.MINI_BOSS_WINDOW);
        }
    }

    private void tickCharge(ServerLevel level) {
        int run = this.attackTick - Attack.CHARGE.impact;
        this.face(this.chargeDir);
        if (run < 0) {
            return;
        }
        if (run == 0) {
            this.golem.triggerAnim("action", "charge_run");
            this.chargeFrom = this.golem.position();
        }
        if (run > 2 && this.golem.horizontalCollision) {
            if (this.wallAhead(level)) {
                CrystalGolemFx.chargeWall(level, this.golem.position().add(this.chargeDir.scale(0.8)).add(0.0, 1.6, 0.0));
                NightbladeShakeMessage.around(level, this.golem.position(), 14.0, 1.1F, 0.4F);
                this.daze(Attack.DAZED.length);
                this.golem.setDeltaMovement(this.chargeDir.scale(-0.3).add(0.0, 0.2, 0.0));
            } else {
                this.stopCharge();
            }
            return;
        }
        double travelled = this.golem.position().subtract(this.chargeFrom).horizontalDistance();
        if (travelled >= CHARGE_LENGTH || run >= RUN_TICKS) {
            this.stopCharge();
            return;
        }
        this.golem.setDeltaMovement(this.chargeDir.x * RUN_SPEED, this.golem.getDeltaMovement().y, this.chargeDir.z * RUN_SPEED);
        if (run % 2 == 0) {
            CrystalGolemFx.chargeStep(level, this.golem);
        }
        float damage = (float)this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        AABB front = this.golem.getBoundingBox().inflate(0.7, 0.0, 0.7).move(this.chargeDir.scale(0.6));
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, front, this::hittable)) {
            if (!this.chargeHit.add(victim.getId())) {
                continue;
            }
            if (victim.hurt(this.golem.damageSources().mobAttack(this.golem), damage)) {
                Vec3 side = new Vec3(-this.chargeDir.z, 0.0, this.chargeDir.x);
                Vec3 off = victim.position().subtract(this.golem.position());
                double sign = off.dot(side) >= 0.0 ? 1.0 : -1.0;
                Vec3 push = this.chargeDir.scale(1.2).add(side.scale(0.7 * sign));
                victim.setDeltaMovement(push.x, 0.55, push.z);
                victim.resetFallDistance();
                victim.hasImpulse = true;
                victim.hurtMarked = true;
                CrystalGolemFx.chargeHit(level, victim);
            }
        }
    }

    private boolean wallAhead(ServerLevel level) {
        Vec3 probe = this.golem.position().add(this.chargeDir.scale(this.golem.getBbWidth() * 0.5 + 0.6));
        BlockPos pos = BlockPos.containing(probe.x, this.golem.getY() + 1.2, probe.z);
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
            && !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private void stopCharge() {
        this.golem.setDeltaMovement(this.golem.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        this.attack = Attack.CHARGE_STOP;
        this.attackTick = 0;
        this.golem.triggerAnim("action", Attack.CHARGE_STOP.animation);
    }

    private void tickWave(ServerLevel level) {
        float radius = Math.min(SLAM_RADIUS, 0.6F + this.waveTick * WAVE_SPEED);
        float damage = CrystalGolemEntity.halfHearts((float)this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * 1.25F);
        AABB box = new AABB(this.waveCentre, this.waveCentre).inflate(radius + 1.0, 3.0, radius + 1.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, this::hittable)) {
            double dx = victim.getX() - this.waveCentre.x;
            double dz = victim.getZ() - this.waveCentre.z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            boolean grounded = victim.onGround() && victim.getY() - this.waveCentre.y < 1.2 && victim.getY() - this.waveCentre.y > -2.0;
            if (distance > radius || !grounded || !this.waveHit.add(victim.getId())) {
                continue;
            }
            if (victim.hurt(this.golem.damageSources().mobAttack(this.golem), damage)) {
                Vec3 out = distance < 0.01 ? this.forward() : new Vec3(dx / distance, 0.0, dz / distance);
                victim.setDeltaMovement(out.x * 0.5, 0.5, out.z * 0.5);
                victim.resetFallDistance();
                victim.hasImpulse = true;
                victim.hurtMarked = true;
                CrystalGolemFx.waveHit(level, victim);
            }
        }
        if (this.waveTick % 2 == 0) {
            CrystalGolemFx.waveFront(level, this.waveCentre, radius);
        }
        this.waveTick = radius >= SLAM_RADIUS ? -1 : this.waveTick + 1;
    }

    private boolean hittable(LivingEntity e) {
        if (e == this.golem || !e.isAlive()) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(e instanceof Monster) || e == this.golem.getTarget();
    }
}
