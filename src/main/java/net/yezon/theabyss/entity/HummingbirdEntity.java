package net.yezon.theabyss.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.jspecify.annotations.Nullable;

public class HummingbirdEntity extends Monster implements GeoEntity {
    public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(HummingbirdEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(HummingbirdEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(HummingbirdEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.hummingbird.idle");
    private static final int FLOWER_RANGE = 8;
    private static final int FLOWER_HEIGHT = 4;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationEvents = "empty";
    private int feedCooldown;
    private boolean legacyPersistent;

    public HummingbirdEntity(EntityType<? extends HummingbirdEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setPathfindingMalus(PathType.FIRE, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGING, -1.0F);
        this.setNoGravity(true);
        this.feedCooldown = 40 + world.getRandom().nextInt(80);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOOT, false);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, "hummingbird");
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, world) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setRequiredPathLength(48.0F);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FeedGoal());
        this.goalSelector.addGoal(2, new WanderGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isPersistenceRequired() {
        return super.isPersistenceRequired() && (!this.legacyPersistent || this.hasCustomName());
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.death"));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Texture", this.getTexture());
        output.putBoolean("SpawnFix", true);
        output.putBoolean("LegacyPersistent", this.legacyPersistent);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Texture").ifPresent(this::setTexture);
        this.legacyPersistent = input.getBooleanOr("LegacyPersistent", !input.getBooleanOr("SpawnFix", false));
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
        if (this.feedCooldown > 0) {
            this.feedCooldown--;
        }
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

    private PlayState movementPredicate(AnimationTest<HummingbirdEntity> state) {
        return this.animationEvents.equals("empty") ? state.setAndContinue(IDLE) : PlayState.STOP;
    }

    private PlayState eventsPredicate(AnimationTest<HummingbirdEntity> state) {
        if (this.animationEvents.equals("empty")) {
            return PlayState.STOP;
        }

        AnimationController<HummingbirdEntity> controller = state.controller();
        if (controller.hasAnimationFinished()) {
            this.animationEvents = "empty";
            controller.reset();
            return PlayState.STOP;
        }

        state.setAnimation(RawAnimation.begin().thenPlay(this.animationEvents));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<HummingbirdEntity>("movement", 4, this::movementPredicate));
        controllers.add(new AnimationController<HummingbirdEntity>("Events", 4, this::eventsPredicate));
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
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.FLYING_SPEED, 0.8);
    }

    private boolean isFood(BlockState state) {
        return state.is(BlockTags.BEE_ATTRACTIVE) || state.is(TheabyssModBlocks.VIGILANT_HANGING_FRUIT.get());
    }

    private boolean fits(Vec3 pos) {
        return this.level().noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(pos)) && this.level().getFluidState(BlockPos.containing(pos)).isEmpty();
    }

    private @Nullable Vec3 hoverSpot(BlockPos flower, BlockState state) {
        double y = state.is(TheabyssModBlocks.VIGILANT_HANGING_FRUIT.get()) ? flower.getY() + 0.1 : flower.getY() + 0.5;
        List<Direction> sides = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Util.shuffle(sides, this.random);
        for (Direction side : sides) {
            Vec3 spot = new Vec3(flower.getX() + 0.5 + side.getStepX() * 0.9, y, flower.getZ() + 0.5 + side.getStepZ() * 0.9);
            if (this.fits(spot)) {
                return spot;
            }
        }
        return null;
    }

    private final class FeedGoal extends Goal {
        private @Nullable BlockPos flower;
        private @Nullable Vec3 spot;
        private @Nullable Path path;
        private int hoverTicks;
        private int timeout;

        FeedGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (HummingbirdEntity.this.feedCooldown > 0 || HummingbirdEntity.this.random.nextInt(10) != 0) {
                return false;
            }
            List<BlockPos> found = new ArrayList<>();
            BlockPos origin = HummingbirdEntity.this.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-FLOWER_RANGE, -FLOWER_HEIGHT, -FLOWER_RANGE), origin.offset(FLOWER_RANGE, FLOWER_HEIGHT, FLOWER_RANGE))) {
                if (HummingbirdEntity.this.isFood(HummingbirdEntity.this.level().getBlockState(pos))) {
                    found.add(pos.immutable());
                }
            }
            for (int tries = 0; tries < 4 && !found.isEmpty(); tries++) {
                BlockPos pos = found.remove(HummingbirdEntity.this.random.nextInt(found.size()));
                Vec3 target = HummingbirdEntity.this.hoverSpot(pos, HummingbirdEntity.this.level().getBlockState(pos));
                if (target == null) {
                    continue;
                }
                Path route = HummingbirdEntity.this.navigation.createPath(BlockPos.containing(target), 0);
                if (route != null && route.canReach()) {
                    this.flower = pos;
                    this.spot = target;
                    this.path = route;
                    return true;
                }
            }
            HummingbirdEntity.this.feedCooldown = 60;
            return false;
        }

        @Override
        public void start() {
            HummingbirdEntity.this.navigation.moveTo(this.path, 0.6);
            this.hoverTicks = 40 + HummingbirdEntity.this.random.nextInt(60);
            this.timeout = 240;
        }

        @Override
        public boolean canContinueToUse() {
            return this.flower != null && this.spot != null && this.timeout > 0 && this.hoverTicks > 0
                && HummingbirdEntity.this.isFood(HummingbirdEntity.this.level().getBlockState(this.flower))
                && HummingbirdEntity.this.fits(this.spot);
        }

        @Override
        public void tick() {
            this.timeout--;
            Vec3 target = this.spot;
            HummingbirdEntity.this.getLookControl().setLookAt(Vec3.atCenterOf(this.flower));
            if (HummingbirdEntity.this.position().distanceToSqr(target) > 0.36) {
                if (HummingbirdEntity.this.navigation.isDone()) {
                    HummingbirdEntity.this.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.4);
                }
                return;
            }
            HummingbirdEntity.this.navigation.stop();
            this.hoverTicks--;
            double t = HummingbirdEntity.this.tickCount * 0.35;
            HummingbirdEntity.this.getMoveControl()
                .setWantedPosition(target.x + Math.sin(t) * 0.06, target.y + Math.sin(t * 1.7) * 0.08, target.z + Math.cos(t * 1.3) * 0.06, 0.2);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.flower = null;
            this.spot = null;
            this.path = null;
            HummingbirdEntity.this.navigation.stop();
            HummingbirdEntity.this.feedCooldown = 160 + HummingbirdEntity.this.random.nextInt(200);
        }
    }

    private final class WanderGoal extends Goal {
        WanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return HummingbirdEntity.this.navigation.isDone() && HummingbirdEntity.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return HummingbirdEntity.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 view = HummingbirdEntity.this.getViewVector(0.0F);
            Vec3 target = HoverRandomPos.getPos(HummingbirdEntity.this, 8, 7, view.x, view.z, (float)(Math.PI / 2), 3, 1);
            if (target == null) {
                target = AirAndWaterRandomPos.getPos(HummingbirdEntity.this, 8, 4, -2, view.x, view.z, Math.PI / 2);
            }
            if (target != null) {
                HummingbirdEntity.this.navigation.moveTo(HummingbirdEntity.this.navigation.createPath(BlockPos.containing(target), 1), 0.7);
            }
        }
    }
}
