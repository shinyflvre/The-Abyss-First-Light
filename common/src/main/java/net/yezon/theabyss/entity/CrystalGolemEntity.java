package net.yezon.theabyss.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.SetMiniBossEvents;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public class CrystalGolemEntity extends Monster implements GeoEntity {
    private static final String ANIM = "animation.crystal_golem.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIM + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ANIM + "walk");
    static final String[] ACTIONS = {"punch", "iron_punch", "slam", "stagger", "eruption", "throw", "charge_windup", "charge_stop",
        "charge_stunned"};
    public static final EntityDataAccessor<Boolean> IRON_FIST = SynchedEntityData.defineId(CrystalGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private float fistGlow;
    private float fistGlowO;
    private CrystalGolemAttackGoal attackGoal;

    public CrystalGolemEntity(EntityType<CrystalGolemEntity> type, Level world) {
        super(type, world);
        this.xpReward = 50;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return distSqr > 128.0 * 128.0 && this.getTarget() == null;
    }

    @Override
    public int getTeamColor() {
        return net.yezon.theabyss.ring.EagleBossTracker.OUTLINE;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.attackGoal = new CrystalGolemAttackGoal(this);
        this.goalSelector.addGoal(1, this.attackGoal);
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IRON_FIST, false);
    }

    public void devHold(boolean hold) {
        if (this.attackGoal != null) {
            this.attackGoal.devHold = hold;
        }
    }

    public void devAttack(String name, net.minecraft.world.entity.LivingEntity target) {
        if (this.attackGoal != null) {
            this.attackGoal.devForce(name, target);
        }
    }

    public boolean isDazed() {
        return this.attackGoal != null && this.attackGoal.dazed();
    }

    void dazeFromReflectedShard() {
        if (this.attackGoal != null) {
            this.attackGoal.daze(50);
        }
    }

    void setIronFist(boolean glowing) {
        this.entityData.set(IRON_FIST, glowing);
    }

    static float halfHearts(float hp) {
        return Math.max(1, Math.round(hp));
    }

    public float fistGlow(float partialTick) {
        return Mth.lerp(partialTick, this.fistGlowO, this.fistGlow);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.fistGlowO = this.fistGlow;
            boolean charging = this.entityData.get(IRON_FIST);
            this.fistGlow = charging ? Math.min(1.0F, this.fistGlow + 0.07F) : Math.max(0.0F, this.fistGlow - 0.25F);
            if (charging) {
                this.fistParticles();
            }
        }
    }

    private void fistParticles() {
        float yaw = this.yBodyRot * ((float)Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        Vec3 left = new Vec3(forward.z, 0.0, -forward.x);
        float k = this.fistGlow;
        Vec3 fist = this.position().add(left.scale(0.7)).add(forward.scale(-1.3 * k)).add(0.0, 1.35 + 0.4 * k, 0.0);
        int count = 1 + (int)(k * 3.0F);
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 0.9;
            double oy = (this.random.nextDouble() - 0.5) * 0.9;
            double oz = (this.random.nextDouble() - 0.5) * 0.9;
            int color = this.random.nextInt(3) == 0 ? CrystalGolemFx.PALE : CrystalGolemFx.PINK;
            this.level().addParticle(TheabyssModParticleTypes.FX_SPARKLE.get().of(color, 0.14F + 0.12F * k, 10, FxOptions.FLICKER),
                fist.x + ox, fist.y + oy, fist.z + oz, -ox * 0.08, 0.02 - oy * 0.06, -oz * 0.08);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<CrystalGolemEntity>("base", 6, this::baseAnimation));
        AnimationController<CrystalGolemEntity> action = new AnimationController<>("action", 3, test -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(ANIM + name));
        }
        action.triggerableAnim("charge_run", RawAnimation.begin().thenLoop(ANIM + "charge_run"));
        controllers.add(action);
    }

    private PlayState baseAnimation(AnimationTest<CrystalGolemEntity> test) {
        if (test.isMoving()) {
            test.controller().setAnimationSpeed(Mth.clamp(this.walkAnimation.speed() * 1.6F, 0.6F, 1.5F));
            return test.setAndContinue(WALK);
        }
        test.controller().setAnimationSpeed(1.0);
        return test.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(level, new ItemStack(TheabyssModItems.CRYSTAL_HAND.get()));
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:crystal_golem_idle"));
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.iron_golem.step")), 0.15F, 1.0F);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:crystal_golem_hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:crystal_golem_die"));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof AbstractArrow
            || damagesource.getDirectEntity() instanceof AbstractThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)) {
            return false;
        }
        if (this.attackGoal != null && this.attackGoal.dazed()) {
            amount = halfHearts(amount * 1.5F);
        }
        return super.hurtServer(level, damagesource, amount);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData livingdata
    ) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
        SetMiniBossEvents.execute(world, this.getX(), this.getY(), this.getZ());
        return retval;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.CRYSTAL_GOLEM.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(world, pos, random)
                && Mob.checkMobSpawnRules(entityType, world, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.23)
            .add(Attributes.MAX_HEALTH, 400.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.ATTACK_DAMAGE, 14.0)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 4.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.0);
    }
}
