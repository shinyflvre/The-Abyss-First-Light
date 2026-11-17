package net.yezon.theabyss.entity;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.echo.SilentEchoes;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.util.RingMode;
import org.jspecify.annotations.Nullable;

public class SilentEchoEntity extends PathfinderMob {
    public static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(SilentEchoEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> VESSEL = SynchedEntityData.defineId(SilentEchoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EquipmentSlot[] MIRRORED = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private static final double CATCH_UP = 24.0;
    private static final int GUARD_MEMORY = 100;

    public SilentEchoEntity(EntityType<? extends SilentEchoEntity> type, Level level) {
        super(type, level);
        for (EquipmentSlot slot : MIRRORED) {
            this.setDropChance(slot, 0.0F);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ATTACK_DAMAGE, 1.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
        builder.define(VESSEL, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BowGoal(this));
        this.goalSelector.addGoal(2, new StrikeGoal(this));
        this.goalSelector.addGoal(3, new FollowGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !SilentEchoEntity.this.isVessel() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !SilentEchoEntity.this.isVessel() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new GuardGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !SilentEchoEntity.this.isVessel() && super.canUse();
            }
        });
    }

    public @Nullable UUID ownerId() {
        String id = this.entityData.get(OWNER);
        if (id.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException broken) {
            return null;
        }
    }

    public @Nullable ServerPlayer owner() {
        UUID id = this.ownerId();
        if (id == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(id);
    }

    public boolean isVessel() {
        return this.entityData.get(VESSEL);
    }

    public void bind(ServerPlayer owner) {
        this.entityData.set(OWNER, owner.getUUID().toString());
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(owner.getMaxHealth());
        }
        this.setHealth(Math.max(1.0F, owner.getHealth()));
        this.mirror(owner);
    }

    public void becomeVessel(Vec3 at, float yaw, float headYaw, float health) {
        this.entityData.set(VESSEL, true);
        this.setTarget(null);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setAggressive(false);
        this.stopUsingItem();
        this.teleportTo(at.x, at.y, at.z);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(headYaw);
        this.setXRot(0.0F);
        this.setHealth(Math.max(1.0F, Math.min(health, this.getMaxHealth())));
    }

    public void leaveVessel(Vec3 at, float yaw, float health) {
        this.entityData.set(VESSEL, false);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.teleportTo(at.x, at.y, at.z);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.setHealth(Math.max(1.0F, Math.min(health, this.getMaxHealth())));
    }

    private void mirror(ServerPlayer owner) {
        for (EquipmentSlot slot : MIRRORED) {
            ItemStack want = owner.getItemBySlot(slot);
            if (!ItemStack.matches(this.getItemBySlot(slot), want)) {
                this.setItemSlot(slot, want.copy());
            }
        }
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getBaseValue() != owner.getMaxHealth()) {
            health.setBaseValue(owner.getMaxHealth());
            if (this.getHealth() > this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.motes();
            return;
        }
        if (this.isRemoved() || this.isDeadOrDying()) {
            return;
        }
        ServerPlayer owner = this.owner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            SilentEchoes.dissolve(this, this.position());
            return;
        }
        if (this.isVessel()) {
            if (!net.yezon.theabyss.ring.echo.EchoVision.isVessel(owner, this)) {
                this.entityData.set(VESSEL, false);
            } else {
                this.getNavigation().stop();
                if (this.tickCount % 20 == 0) {
                    this.lure();
                }
            }
        } else if (owner.level() != this.level()) {
            SilentEchoes.dissolve(this, this.position());
            return;
        } else if (this.distanceToSqr(owner) > CATCH_UP * CATCH_UP) {
            this.catchUp(owner);
        }
        if (this.tickCount % 5 == 0) {
            this.mirror(owner);
        }
    }

    private void motes() {
        if (this.random.nextInt(this.isVessel() ? 4 : 9) != 0) {
            return;
        }
        double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.6;
        double y = this.getY() + this.random.nextDouble() * this.getBbHeight();
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 1.6;
        int colour = this.random.nextBoolean() ? SilentEchoes.PEARL : SilentEchoes.LILAC;
        FxOptions mote = TheabyssModParticleTypes.FX_SPARKLE.get().of(colour, 0.07F, 26, FxOptions.FLICKER).gravity(-0.002F);
        this.level().addParticle(mote, x, y, z, 0.0, 0.01, 0.0);
    }

    private void lure() {
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(16.0),
            m -> m.isAlive() && m.getTarget() == null && m.hasLineOfSight(this))) {
            monster.setTarget(this);
        }
    }

    private void catchUp(ServerPlayer owner) {
        for (int i = 0; i < 12; i++) {
            double x = owner.getX() + this.random.nextIntBetweenInclusive(-3, 3);
            double z = owner.getZ() + this.random.nextIntBetweenInclusive(-3, 3);
            double y = owner.getY() + this.random.nextIntBetweenInclusive(-1, 1);
            BlockPos below = BlockPos.containing(x, y - 0.5, z);
            if (Math.abs(x - owner.getX()) < 1.5 && Math.abs(z - owner.getZ()) < 1.5) {
                continue;
            }
            if (!this.level().getBlockState(below).isFaceSturdy(this.level(), below, net.minecraft.core.Direction.UP)) {
                continue;
            }
            Vec3 at = new Vec3(Math.floor(x) + 0.5, Math.floor(y), Math.floor(z) + 0.5);
            if (this.level().noCollision(this, this.getBoundingBox().move(at.subtract(this.position())))) {
                SilentEchoes.blink(this, at);
                this.getNavigation().stop();
                return;
            }
        }
    }

    public boolean allowedTarget(@Nullable LivingEntity target) {
        if (target == null) {
            return true;
        }
        if (target == this || !target.isAlive() || target instanceof SilentEchoEntity) {
            return false;
        }
        UUID owner = this.ownerId();
        if (owner == null || owner.equals(target.getUUID())) {
            return false;
        }
        if (target instanceof OwnableEntity pet && pet.getOwnerUUID() != null && owner.equals(pet.getOwnerUUID())) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        ServerPlayer caster = this.owner();
        return caster == null || !RingMode.spares(caster, target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(this.allowedTarget(target) ? target : null);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        UUID owner = this.ownerId();
        if (owner != null && source.getEntity() != null && owner.equals(source.getEntity().getUUID())) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide()) {
            SilentEchoes.dissolve(this, this.position());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            SilentEchoes.gone(this);
        }
    }

    @Override
    protected int getBaseExperienceReward() {
        return 0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    boolean holdsBow() {
        ItemStack hand = this.getMainHandItem();
        return hand.getItem() instanceof BowItem || hand.getItem() instanceof CrossbowItem;
    }

    private static final class StrikeGoal extends MeleeAttackGoal {
        private final SilentEchoEntity echo;

        StrikeGoal(SilentEchoEntity echo) {
            super(echo, 1.25, true);
            this.echo = echo;
        }

        @Override
        public boolean canUse() {
            return !this.echo.isVessel() && !this.echo.holdsBow() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.echo.isVessel() && !this.echo.holdsBow() && super.canContinueToUse();
        }
    }

    private static final class BowGoal extends Goal {
        private final SilentEchoEntity echo;
        private int reload;
        private int seen;

        BowGoal(SilentEchoEntity echo) {
            this.echo = echo;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.echo.getTarget();
            return !this.echo.isVessel() && this.echo.holdsBow() && target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.echo.setAggressive(true);
            this.reload = 20;
        }

        @Override
        public void stop() {
            this.echo.setAggressive(false);
            this.seen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.echo.getTarget();
            if (target == null) {
                return;
            }
            double distance = this.echo.distanceToSqr(target);
            boolean sees = this.echo.getSensing().hasLineOfSight(target);
            this.seen = sees ? this.seen + 1 : 0;
            if (distance > 14.0 * 14.0 || !sees) {
                this.echo.getNavigation().moveTo(target, 1.2);
            } else if (distance < 5.0 * 5.0) {
                Vec3 away = this.echo.position().subtract(target.position()).normalize().scale(4.0).add(this.echo.position());
                this.echo.getNavigation().moveTo(away.x, away.y, away.z, 1.2);
            } else {
                this.echo.getNavigation().stop();
            }
            this.echo.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.reload <= 0 && sees && this.seen > 5) {
                this.shoot(target);
                this.reload = 30;
            }
        }

        private void shoot(LivingEntity target) {
            ItemStack weapon = this.echo.getMainHandItem();
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this.echo, new ItemStack(Items.ARROW), 1.0F, weapon);
            double dx = target.getX() - this.echo.getX();
            double dy = target.getY(0.3333) - arrow.getY();
            double dz = target.getZ() - this.echo.getZ();
            double flat = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + flat * 0.2, dz, 1.6F, 2.0F);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            this.echo.level().addFreshEntity(arrow);
            this.echo.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.echo.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    private static final class FollowGoal extends Goal {
        private final SilentEchoEntity echo;
        private ServerPlayer owner;

        FollowGoal(SilentEchoEntity echo) {
            this.echo = echo;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.echo.isVessel() || this.echo.getTarget() != null) {
                return false;
            }
            ServerPlayer owner = this.echo.owner();
            if (owner == null || owner.level() != this.echo.level() || this.echo.distanceToSqr(owner) < 4.0 * 4.0) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.echo.isVessel() && this.echo.getTarget() == null && this.owner != null && this.owner.isAlive()
                && this.owner.level() == this.echo.level() && this.echo.distanceToSqr(this.owner) > 2.5 * 2.5;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.echo.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }
            this.echo.getLookControl().setLookAt(this.owner, 10.0F, this.echo.getMaxHeadXRot());
            if (this.echo.tickCount % 5 == 0) {
                double speed = this.echo.distanceToSqr(this.owner) > 10.0 * 10.0 ? 1.5 : 1.2;
                this.echo.getNavigation().moveTo(this.owner, speed);
            }
        }
    }

    private static final class GuardGoal extends TargetGoal {
        private final SilentEchoEntity echo;
        private LivingEntity found;

        GuardGoal(SilentEchoEntity echo) {
            super(echo, false);
            this.echo = echo;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.echo.isVessel()) {
                return false;
            }
            ServerPlayer owner = this.echo.owner();
            if (owner == null || owner.level() != this.echo.level()) {
                return false;
            }
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && owner.tickCount - owner.getLastHurtByMobTimestamp() < GUARD_MEMORY && this.fits(attacker)) {
                this.found = attacker;
                return true;
            }
            LivingEntity victim = owner.getLastHurtMob();
            if (victim != null && owner.tickCount - owner.getLastHurtMobTimestamp() < GUARD_MEMORY && this.fits(victim)) {
                this.found = victim;
                return true;
            }
            for (Monster monster : this.echo.level().getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(12.0),
                m -> m.isAlive() && m.getTarget() == owner)) {
                if (this.fits(monster)) {
                    this.found = monster;
                    return true;
                }
            }
            return false;
        }

        private boolean fits(LivingEntity target) {
            return target != this.echo.getTarget() && this.echo.allowedTarget(target) && this.echo.distanceToSqr(target) < 24.0 * 24.0;
        }

        @Override
        public void start() {
            this.echo.setTarget(this.found);
            this.targetMob = this.found;
            super.start();
        }
    }
}
