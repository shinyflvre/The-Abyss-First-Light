package net.yezon.theabyss.entity;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class LurkerAttackGoal extends Goal {
    enum Attack {
        BITE("bite", 7, 14),
        SPIT("spit", 12, 24),
        POUNCE("pounce", 8, 28);

        final String animation;
        final int impact;
        final int length;

        Attack(String animation, int impact, int length) {
            this.animation = animation;
            this.impact = impact;
            this.length = length;
        }
    }

    private static final double BITE_REACH = 2.6;
    private static final double SPIT_MIN = 4.0;
    private static final double SPIT_MAX = 16.0;
    private static final double SPIT_SPEED = 0.85;
    private static final double STALK_MIN = 8.0;
    private static final double STALK_MAX = 24.0;
    private static final double STALK_STRIKE = 9.0;
    private static final int STALK_LIMIT = 240;
    private static final double LEAP_STEP = 1.1;
    private static final double LEAP_HEIGHT = 1.3;
    private static final double LEAP_SPEED = 0.85;
    private static final int LEAP_LIMIT = 16;

    private final JungleLurkerEntity lurker;
    private Attack attack;
    private int attackTick;
    private int cooldown;
    private int spitCooldown = 40;
    private int pounceCooldown = 80;
    private int ambushCooldown = 60;
    private int stalkTicks;
    private int repath;
    private LivingEntity attackTarget;
    private Vec3 leapDir = Vec3.ZERO;
    private Vec3 leapStart = Vec3.ZERO;
    private int leapTicks;
    private boolean leapDone;
    private boolean ambushLeap;

    public LurkerAttackGoal(JungleLurkerEntity lurker) {
        this.lurker = lurker;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.lurker.getTarget();
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
        this.attack = null;
        this.lurker.getNavigation().stop();
        if (this.lurker.isCamouflaged() && this.lurker.level() instanceof ServerLevel level) {
            this.reveal(level, false);
        }
    }

    void reveal(ServerLevel level, boolean loud) {
        if (!this.lurker.isCamouflaged()) {
            return;
        }
        this.lurker.setCamouflaged(false);
        this.stalkTicks = 0;
        this.ambushCooldown = 500 + this.lurker.getRandom().nextInt(200);
        LurkerFx.fade(level, this.lurker, false);
        if (!loud) {
            this.cooldown = Math.max(this.cooldown, 10);
        }
    }

    private float damage(float factor) {
        return Math.max(1, Math.round((float)this.lurker.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor));
    }

    @Override
    public void tick() {
        if (!(this.lurker.level() instanceof ServerLevel level)) {
            return;
        }
        this.cooldown--;
        this.spitCooldown--;
        this.pounceCooldown--;
        this.ambushCooldown--;
        if (this.attack != null) {
            this.tickAttack(level);
            return;
        }
        LivingEntity target = this.lurker.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.lurker.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double reach = this.reachTo(target);
        double dy = Math.abs(target.getY() - this.lurker.getY());
        boolean sees = this.lurker.getSensing().hasLineOfSight(target);
        if (this.lurker.isCamouflaged()) {
            this.stalkTicks++;
            this.chase(target, 0.75);
            if (sees && reach <= STALK_STRIKE && dy <= 2.5) {
                this.begin(Attack.POUNCE, target);
            } else if (this.stalkTicks > STALK_LIMIT) {
                this.reveal(level, false);
            }
            return;
        }
        if (this.ambushCooldown <= 0 && reach >= STALK_MIN && reach <= STALK_MAX && this.lurker.hurtTime == 0
            && this.lurker.tickCount - this.lurker.getLastHurtByMobTimestamp() > 100) {
            this.lurker.setCamouflaged(true);
            this.stalkTicks = 0;
            LurkerFx.fade(level, this.lurker, true);
            return;
        }
        this.chase(target, 1.0);
        if (this.cooldown <= 0 && sees) {
            Attack next = this.choose(target, reach, dy);
            if (next != null) {
                this.begin(next, target);
            }
        }
    }

    private void chase(LivingEntity target, double speed) {
        if (--this.repath <= 0 || this.lurker.getNavigation().isDone()) {
            this.repath = 10 + this.lurker.getRandom().nextInt(10);
            this.lurker.getNavigation().moveTo(target, speed);
        }
    }

    private Attack choose(LivingEntity target, double reach, double dy) {
        int bite = reach <= BITE_REACH && dy <= 2.0 && this.facing(target) > 0.3 ? 60 : 0;
        int spit = reach >= SPIT_MIN && reach <= SPIT_MAX && this.spitCooldown <= 0 ? 40 : 0;
        int pounce = reach >= 4.5 && reach <= 8.0 && dy <= 2.0 && this.pounceCooldown <= 0 ? 15 : 0;
        int total = bite + spit + pounce;
        if (total <= 0) {
            return null;
        }
        int roll = this.lurker.getRandom().nextInt(total);
        if (roll < bite) {
            return Attack.BITE;
        }
        return roll < bite + spit ? Attack.SPIT : Attack.POUNCE;
    }

    private void begin(Attack next, LivingEntity target) {
        this.attack = next;
        this.attackTick = 0;
        this.attackTarget = target;
        this.lurker.getNavigation().stop();
        this.lurker.triggerAnim("action", next.animation);
        switch (next) {
            case SPIT -> this.spitCooldown = 100 + this.lurker.getRandom().nextInt(60);
            case POUNCE -> {
                this.pounceCooldown = 160 + this.lurker.getRandom().nextInt(80);
                this.leapDone = false;
                this.ambushLeap = this.lurker.isCamouflaged();
            }
            default -> {
            }
        }
    }

    private void tickAttack(ServerLevel level) {
        Attack current = this.attack;
        this.attackTick++;
        LivingEntity target = this.attackTarget;
        boolean aim = target != null && target.isAlive();
        if (current != Attack.POUNCE || this.attackTick < current.impact) {
            this.lurker.getNavigation().stop();
        }
        switch (current) {
            case BITE -> {
                if (aim && this.attackTick < current.impact - 1) {
                    this.face(target.position().subtract(this.lurker.position()));
                }
                if (this.attackTick == current.impact) {
                    boolean hit = aim && this.reachTo(target) <= BITE_REACH + 0.4 && this.facing(target) > 0.2
                        && Math.abs(target.getY() - this.lurker.getY()) < 2.5 && this.strike(level, target, this.damage(1.0F));
                    LurkerFx.bite(level, this.jaws(), hit);
                }
            }
            case SPIT -> {
                if (aim && this.attackTick < current.impact) {
                    this.face(target.position().subtract(this.lurker.position()));
                }
                if (this.attackTick == 4) {
                    LurkerFx.spitCharge(level, this.jaws().add(0.0, -0.3, 0.0));
                }
                if (this.attackTick == current.impact && aim) {
                    this.spit(level, target);
                }
            }
            case POUNCE -> this.tickPounce(level, target);
        }
        if (this.attack == current && this.attackTick >= current.length) {
            this.attack = null;
            this.cooldown = current == Attack.POUNCE && this.leapDone && this.ambushLeap ? 0 : 8 + this.lurker.getRandom().nextInt(10);
        }
    }

    private void spit(ServerLevel level, LivingEntity target) {
        Vec3 from = this.jaws();
        Vec3 aim = target.position().add(target.getDeltaMovement().scale(6.0)).add(0.0, target.getBbHeight() * 0.55, 0.0);
        Vec3 to = aim.subtract(from);
        double flat = to.horizontalDistance();
        double ticks = Mth.clamp(flat / SPIT_SPEED, 6.0, 24.0);
        Vec3 velocity = new Vec3(to.x / ticks, to.y / ticks + 0.5 * LurkerSpitEntity.GRAVITY * (ticks + 1.0), to.z / ticks);
        LurkerSpitEntity glob = new LurkerSpitEntity(net.yezon.theabyss.init.TheabyssModEntities.LURKER_SPIT.get(), level);
        glob.setPos(from.x, from.y, from.z);
        glob.launch(this.lurker, velocity);
        level.addFreshEntity(glob);
        LurkerFx.spit(level, from, velocity.normalize());
    }

    private void tickPounce(ServerLevel level, LivingEntity target) {
        int t = this.attackTick - Attack.POUNCE.impact;
        boolean aim = target != null && target.isAlive();
        if (t < 0) {
            if (aim) {
                this.face(target.position().subtract(this.lurker.position()));
            }
            return;
        }
        if (t == 0) {
            if (this.lurker.isCamouflaged()) {
                this.reveal(level, true);
            }
            this.leapDir = this.forward();
            this.leapStart = this.lurker.position();
            double reach = aim ? this.reachTo(target) : 4.0;
            this.leapTicks = Mth.clamp((int)Math.ceil(reach / LEAP_STEP), 4, 10);
            if (!this.ambushLeap) {
                double speed = Mth.clamp(reach / 7.0, 0.5, 1.0) * LEAP_SPEED;
                this.lurker.setDeltaMovement(this.leapDir.x * speed, 0.42, this.leapDir.z * speed);
                this.lurker.hasImpulse = true;
            }
            LurkerFx.leap(level, this.lurker);
        }
        if (!this.ambushLeap) {
            this.tickPlainLeap(level, target, t, aim);
            return;
        }
        if (t >= this.leapTicks || this.leapDone) {
            return;
        }
        Vec3 pos = this.lurker.position();
        Vec3 goal;
        if (aim) {
            Vec3 to = target.position().subtract(pos);
            Vec3 flat = new Vec3(to.x, 0.0, to.z);
            double stop = Math.max(0.0, flat.length() - 0.7);
            goal = flat.lengthSqr() > 1.0E-4 ? pos.add(flat.normalize().scale(stop)) : pos;
            goal = new Vec3(goal.x, target.getY(), goal.z);
            this.face(to);
        } else {
            goal = pos.add(this.leapDir.scale(LEAP_STEP));
        }
        int left = this.leapTicks - t;
        Vec3 step = new Vec3((goal.x - pos.x) / left, 0.0, (goal.z - pos.z) / left);
        double flatStep = step.horizontalDistance();
        if (flatStep > LEAP_STEP * 1.4) {
            step = step.scale(LEAP_STEP * 1.4 / flatStep);
        }
        double s1 = (t + 1) / (double)this.leapTicks;
        double baseY = Mth.lerp(s1, this.leapStart.y, goal.y);
        double arc = 4.0 * LEAP_HEIGHT * s1 * (1.0 - s1);
        double dy = baseY + arc - pos.y;
        this.lurker.setDeltaMovement(step.x, Mth.clamp(dy, -0.9, 0.9), step.z);
        this.lurker.hasImpulse = true;
        this.lurker.resetFallDistance();
        if (aim && (this.lurker.getBoundingBox().inflate(0.9, 0.5, 0.9).intersects(target.getBoundingBox()) || this.reachTo(target) <= 1.4
            || s1 >= 0.999)) {
            if (this.reachTo(target) > 2.2 && s1 < 0.999) {
                return;
            }
            this.leapDone = true;
            boolean hit = this.reachTo(target) <= 2.2 && Math.abs(target.getY() - this.lurker.getY()) < 2.5
                && this.strike(level, target, this.damage(this.ambushLeap ? 1.5F : 1.0F));
            if (hit) {
                Vec3 push = this.leapDir.scale(0.7);
                target.push(push.x, 0.3, push.z);
                target.hasImpulse = true;
            }
            this.lurker.setDeltaMovement(this.lurker.getDeltaMovement().multiply(0.2, 1.0, 0.2));
            LurkerFx.bite(level, this.jaws(), hit);
        }
    }

    private void tickPlainLeap(ServerLevel level, LivingEntity target, int t, boolean aim) {
        if (this.leapDone || t > LEAP_LIMIT || !aim) {
            return;
        }
        if (this.lurker.getBoundingBox().inflate(0.8, 0.4, 0.8).intersects(target.getBoundingBox()) || this.reachTo(target) <= 1.2) {
            this.leapDone = true;
            boolean hit = this.strike(level, target, this.damage(1.0F));
            if (hit) {
                Vec3 push = this.leapDir.scale(0.6);
                target.push(push.x, 0.25, push.z);
                target.hasImpulse = true;
            }
            LurkerFx.bite(level, this.jaws(), hit);
        }
    }

    private boolean strike(ServerLevel level, LivingEntity victim, float amount) {
        return this.hittable(victim) && victim.hurt(this.lurker.damageSources().mobAttack(this.lurker), amount);
    }

    boolean hittable(LivingEntity e) {
        if (e == this.lurker || !e.isAlive()) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return !(e instanceof Monster) || e == this.lurker.getTarget();
    }

    Vec3 forward() {
        float yaw = this.lurker.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private Vec3 jaws() {
        return this.lurker.position().add(this.forward().scale(0.8)).add(0.0, 1.55, 0.0);
    }

    private double reachTo(LivingEntity target) {
        double dx = target.getX() - this.lurker.getX();
        double dz = target.getZ() - this.lurker.getZ();
        return Math.sqrt(dx * dx + dz * dz) - target.getBbWidth() * 0.5;
    }

    private double facing(LivingEntity e) {
        Vec3 to = new Vec3(e.getX() - this.lurker.getX(), 0.0, e.getZ() - this.lurker.getZ());
        return to.lengthSqr() < 1.0E-4 ? 1.0 : to.normalize().dot(this.forward());
    }

    private void face(Vec3 dir) {
        if (dir.horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        float want = (float)(Math.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        float yaw = this.lurker.getYRot() + Mth.clamp(Mth.wrapDegrees(want - this.lurker.getYRot()), -20.0F, 20.0F);
        this.lurker.setYRot(yaw);
        this.lurker.setYBodyRot(yaw);
        this.lurker.setYHeadRot(yaw);
    }
}
