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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.EventHooks;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public class LizardEntity extends TamableAnimal implements GeoEntity {
    private static final EntityDataAccessor<Byte> DATA_WALL = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BYTE);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.lizard.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.lizard.walk");
    private static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("animation.lizard.climb");
    private static final RawAnimation CLING = RawAnimation.begin().thenLoop("animation.lizard.cling");
    private static final double HALF_WIDTH = 0.4;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public float wallBlend;
    public float wallBlendO;
    public int lastWall = 2;

    public LizardEntity(EntityType<LizardEntity> type, Level world) {
        super(type, world);
        this.xpReward = 2;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WALL, (byte)-1);
    }

    public @Nullable Direction wall() {
        byte b = this.entityData.get(DATA_WALL);
        return b < 0 ? null : Direction.from3DDataValue(b);
    }

    public void setWall(@Nullable Direction wall) {
        this.entityData.set(DATA_WALL, wall == null ? (byte)-1 : (byte)wall.get3DDataValue());
        this.setNoGravity(wall != null);
        if (wall != null) {
            this.face(wall);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    void face(Direction wall) {
        float yaw = wall.toYRot();
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    static boolean climbable(BlockGetter level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, side) && state.is(BlockTags.LOGS);
    }

    boolean wallAt(Direction wall, double up) {
        BlockPos p = BlockPos.containing(this.getX(), this.getY() + up, this.getZ()).relative(wall);
        return climbable(this.level(), p, wall.getOpposite());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new WallGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.goalSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, stack -> stack.is(TheabyssModItems.LORAN.get()), false));
        this.goalSelector.addGoal(5, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(6, new SeekTreeGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(7, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.wallBlendO = this.wallBlend;
            if (this.wall() != null) {
                this.lastWall = this.wall().get3DDataValue();
            }
            float target = this.wall() != null ? 1.0F : 0.0F;
            this.wallBlend += Mth.clamp(target - this.wallBlend, -0.2F, 0.2F);
            return;
        }
        Direction wall = this.wall();
        if (wall == null && this.horizontalCollision && !this.isPassenger() && !this.isInWater() && !this.isOrderedToSit() && this.getTarget() == null
            && this.random.nextInt(3) == 0) {
            Vec3 motion = this.getDeltaMovement();
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (this.wallAt(d, 0.3) && d.getStepX() * motion.x + d.getStepZ() * motion.z >= -0.001) {
                    this.setWall(d);
                    break;
                }
            }
        }
        if (this.wall() != null && (this.isInWater() || this.isPassenger() || this.isOrderedToSit())) {
            this.setWall(null);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                                  @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if ((reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) && this.random.nextFloat() < 0.6F) {
            this.clingToNearbyTree(level);
        }
        return result;
    }

    private void clingToNearbyTree(LevelAccessor level) {
        BlockPos feet = this.blockPosition();
        List<BlockPos> cells = new ArrayList<>();
        List<Direction> walls = new ArrayList<>();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    p.set(feet.getX() + dx, feet.getY() + dy, feet.getZ() + dz);
                    if (!level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()) {
                        continue;
                    }
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        if (climbable(level, p.relative(d), d.getOpposite())) {
                            cells.add(p.immutable());
                            walls.add(d);
                            break;
                        }
                    }
                }
            }
        }
        if (cells.isEmpty()) {
            return;
        }
        int i = this.random.nextInt(cells.size());
        BlockPos cell = cells.get(i);
        Direction wall = walls.get(i);
        double gap = 0.5 - HALF_WIDTH - 0.01;
        this.moveTo(cell.getX() + 0.5 + wall.getStepX() * gap, cell.getY() + 0.1, cell.getZ() + 0.5 + wall.getStepZ() * gap, wall.toYRot(), 0.0F);
        this.setWall(wall);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag outputTag) {
        ValueOutput output = ValueOutput.of(outputTag, this.registryAccess());
        super.addAdditionalSaveData(output.tag());
        Direction wall = this.wall();
        output.putInt("Wall", wall == null ? -1 : wall.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag inputTag) {
        ValueInput input = ValueInput.of(inputTag, this.registryAccess());
        super.readAdditionalSaveData(input.tag());
        int wall = input.getIntOr("Wall", -1);
        if (wall >= 2 && wall <= 5) {
            this.entityData.set(DATA_WALL, (byte)wall);
            this.setNoGravity(true);
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:lizard_idle"));
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
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)) {
            return false;
        }
        boolean hurt = super.hurt(damagesource, amount);
        if (hurt && this.wall() != null) {
            this.setWall(null);
        }
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player sourceentity, InteractionHand hand) {
        ItemStack itemstack = sourceentity.getItemInHand(hand);
        InteractionResult retval = InteractionResult.SUCCESS;
        if (itemstack.getItem() instanceof SpawnEggItem) {
            retval = super.mobInteract(sourceentity, hand);
        } else if (this.level().isClientSide()) {
            retval = (!this.isTame() || !this.isOwnedBy(sourceentity)) && !this.isFood(itemstack) ? InteractionResult.PASS : InteractionResult.SUCCESS;
        } else if (this.isTame()) {
            if (this.isOwnedBy(sourceentity)) {
                FoodProperties food = itemstack.get(DataComponents.FOOD);
                if (food != null && this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    this.usePlayerItem(sourceentity, hand, itemstack);
                    this.heal(food.nutrition());
                    retval = InteractionResult.SUCCESS;
                } else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    this.usePlayerItem(sourceentity, hand, itemstack);
                    this.heal(4.0F);
                    retval = InteractionResult.SUCCESS;
                } else {
                    retval = super.mobInteract(sourceentity, hand);
                }
            }
        } else if (this.isFood(itemstack)) {
            this.usePlayerItem(sourceentity, hand, itemstack);
            if (this.random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, sourceentity)) {
                this.tame(sourceentity);
                this.level().broadcastEntityEvent(this, (byte)7);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
            }

            this.setPersistenceRequired();
            retval = InteractionResult.SUCCESS;
        } else {
            retval = super.mobInteract(sourceentity, hand);
            if (retval.consumesAction()) {
                this.setPersistenceRequired();
            }
        }

        return retval;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        LizardEntity retval = TheabyssModEntities.LIZARD.get().create(serverWorld);
        if (retval != null) {
            retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null);
        }
        return retval;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TheabyssModItems.LORAN.get());
    }

    private PlayState movement(AnimationState<LizardEntity> state) {
        boolean climbing = this.wall() != null;
        boolean moving = climbing ? Math.abs(this.getY() - this.yo) > 0.004 : state.isMoving();
        if (climbing) {
            return state.setAndContinue(moving ? CLIMB : CLING);
        }
        return state.setAndContinue(moving ? WALK : IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<LizardEntity>(this, "movement", 4, this::movement));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.LIZARD.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> Mob.checkMobSpawnRules(entityType, world, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 7.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return true;
    }

    static final class WallGoal extends Goal {
        private enum Mode { REST, UP, DOWN }

        private final LizardEntity lizard;
        private Mode mode = Mode.REST;
        private int timer;

        WallGoal(LizardEntity lizard) {
            this.lizard = lizard;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.lizard.wall() != null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.pick();
        }

        private void pick() {
            int r = this.lizard.getRandom().nextInt(10);
            if (this.mode != Mode.REST) {
                this.mode = Mode.REST;
                this.timer = 60 + this.lizard.getRandom().nextInt(160);
            } else if (r < 5) {
                this.mode = Mode.UP;
                this.timer = 15 + this.lizard.getRandom().nextInt(45);
            } else {
                this.mode = Mode.DOWN;
                this.timer = 200;
            }
        }

        @Override
        public void tick() {
            Direction wall = this.lizard.wall();
            if (wall == null) {
                return;
            }
            this.lizard.face(wall);
            this.lizard.getNavigation().stop();
            if (--this.timer <= 0) {
                this.pick();
            }
            double push = 0.04;
            double vy = switch (this.mode) {
                case REST -> 0.0;
                case UP -> 0.075;
                case DOWN -> -0.075;
            };
            if (this.mode == Mode.UP && !this.lizard.wallAt(wall, 0.3)) {
                BlockPos ledge = BlockPos.containing(this.lizard.getX(), this.lizard.getY() + 0.3, this.lizard.getZ()).relative(wall);
                boolean free = this.lizard.level().getBlockState(ledge).getCollisionShape(this.lizard.level(), ledge).isEmpty()
                    && this.lizard.level().getBlockState(ledge.above()).getCollisionShape(this.lizard.level(), ledge.above()).isEmpty();
                this.lizard.setWall(null);
                if (free) {
                    this.lizard.setDeltaMovement(wall.getStepX() * 0.22, 0.32, wall.getStepZ() * 0.22);
                }
                return;
            }
            if (this.mode == Mode.UP && this.lizard.verticalCollision) {
                this.pick();
            }
            if (this.mode == Mode.DOWN && (this.lizard.onGround() || !this.lizard.wallAt(wall, 0.3))) {
                this.lizard.setWall(null);
                float yaw = wall.getOpposite().toYRot();
                this.lizard.setYRot(yaw);
                this.lizard.setYBodyRot(yaw);
                this.lizard.setDeltaMovement(-wall.getStepX() * 0.2, 0.05, -wall.getStepZ() * 0.2);
                return;
            }
            this.lizard.setDeltaMovement(wall.getStepX() * push, vy, wall.getStepZ() * push);
        }
    }

    static final class SeekTreeGoal extends Goal {
        private final LizardEntity lizard;
        private BlockPos cell;
        private Direction wall;

        SeekTreeGoal(LizardEntity lizard) {
            this.lizard = lizard;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.lizard.wall() != null || !this.lizard.onGround() || this.lizard.isOrderedToSit() || this.lizard.getTarget() != null
                || this.lizard.getRandom().nextInt(160) != 0) {
                return false;
            }
            BlockPos feet = this.lizard.blockPosition();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 24; i++) {
                p.set(feet.getX() + this.lizard.getRandom().nextInt(13) - 6, feet.getY(), feet.getZ() + this.lizard.getRandom().nextInt(13) - 6);
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (this.lizard.level().getBlockState(p).isAir() && climbable(this.lizard.level(), p.relative(d), d.getOpposite())) {
                        this.cell = p.immutable();
                        this.wall = d;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.lizard.getNavigation().moveTo(this.cell.getX() + 0.5, this.cell.getY(), this.cell.getZ() + 0.5, 1.0);
        }

        @Override
        public boolean canContinueToUse() {
            return this.lizard.wall() == null && !this.lizard.getNavigation().isDone();
        }

        @Override
        public void tick() {
            if (this.lizard.position().distanceToSqr(Vec3.atBottomCenterOf(this.cell)) < 0.6 * 0.6 && this.lizard.wallAt(this.wall, 0.3)) {
                this.lizard.setWall(this.wall);
            }
        }

        @Override
        public void stop() {
            if (this.lizard.wall() == null && this.cell != null && this.lizard.position().distanceToSqr(Vec3.atBottomCenterOf(this.cell)) < 1.0
                && this.lizard.wallAt(this.wall, 0.3)) {
                this.lizard.setWall(this.wall);
            }
        }
    }
}
