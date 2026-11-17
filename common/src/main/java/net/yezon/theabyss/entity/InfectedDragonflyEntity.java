package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;

public class InfectedDragonflyEntity extends Monster implements GeoEntity {
    public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(InfectedDragonflyEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(InfectedDragonflyEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(InfectedDragonflyEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.dragonfly.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationEvents = "empty";

    public InfectedDragonflyEntity(EntityType<InfectedDragonflyEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOOT, false);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, "dragonfly");
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new FlyingPathNavigation(this, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0, 20) {
            @Override
            protected Vec3 getPosition() {
                RandomSource random = InfectedDragonflyEntity.this.getRandom();
                double dirX = InfectedDragonflyEntity.this.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                double dirY = InfectedDragonflyEntity.this.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                double dirZ = InfectedDragonflyEntity.this.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                return new Vec3(dirX, dirY, dirZ);
            }
        });
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new FloatGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.5F));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag outputTag) {
        ValueOutput output = ValueOutput.of(outputTag, this.registryAccess());
        super.addAdditionalSaveData(output.tag());
        output.putString("Texture", this.getTexture());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag inputTag) {
        ValueInput input = ValueInput.of(inputTag, this.registryAccess());
        super.readAdditionalSaveData(input.tag());
        input.getString("Texture").ifPresent(this::setTexture);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.refreshDimensions();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 20) {
            this.remove(RemovalReason.KILLED);
        }
    }

    public String getSyncedAnimation() {
        return this.entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        this.entityData.set(ANIMATION, animation);
    }

    private PlayState movementPredicate(AnimationState<InfectedDragonflyEntity> state) {
        return this.animationEvents.equals("empty") ? state.setAndContinue(IDLE) : PlayState.STOP;
    }

    private PlayState eventsPredicate(AnimationState<InfectedDragonflyEntity> state) {
        if (this.animationEvents.equals("empty")) {
            return PlayState.STOP;
        }

        AnimationController<InfectedDragonflyEntity> controller = state.getController();
        if (controller.hasAnimationFinished()) {
            this.animationEvents = "empty";
            controller.forceAnimationReset();
            return PlayState.STOP;
        }

        state.setAnimation(RawAnimation.begin().thenPlay(this.animationEvents));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<InfectedDragonflyEntity>(this, "movement", 4, this::movementPredicate));
        controllers.add(new AnimationController<InfectedDragonflyEntity>(this, "Events", 4, this::eventsPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.8)
            .add(Attributes.MAX_HEALTH, 15.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.FLYING_SPEED, 0.8);
    }
}
