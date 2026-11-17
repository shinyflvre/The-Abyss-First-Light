package net.yezon.theabyss.block.cave;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.mixin.accessor.AbstractCauldronBlockInvoker;
import net.yezon.theabyss.platform.services.BlockItemHooks;
import org.jspecify.annotations.Nullable;

public class AbyssPointedDripstoneBlock extends PointedDripstoneBlock {
    public static final TagKey<Block> SPELEOTHEMS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "speleothems"));
    private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.5F;
    private static final float STALAGMITE_FALL_DAMAGE_MODIFIER = 2.0F;
    private static final double STALACTITE_DRIP_START_PIXEL = 0.3125;
    private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private final Supplier<? extends Block> blockToGrowOn;

    public AbyssPointedDripstoneBlock(Supplier<? extends Block> blockToGrowOn, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockToGrowOn = blockToGrowOn;
    }

    public static boolean isSpeleothem(BlockState state) {
        return state.is(SPELEOTHEMS) && state.hasProperty(TIP_DIRECTION) && state.hasProperty(THICKNESS) && state.hasProperty(WATERLOGGED);
    }

    public static boolean isSpeleothemWithDirection(BlockState state, Direction tipDirection) {
        return isSpeleothem(state) && state.getValue(TIP_DIRECTION) == tipDirection;
    }

    public static boolean isSpeleothemTip(BlockState state, boolean includeMergedTip) {
        if (!isSpeleothem(state)) {
            return false;
        }
        DripstoneThickness thickness = state.getValue(THICKNESS);
        return thickness == DripstoneThickness.TIP || includeMergedTip && thickness == DripstoneThickness.TIP_MERGE;
    }

    public static boolean isFreeHangingStalactite(BlockState state) {
        return isStalactite(state) && state.getValue(THICKNESS) == DripstoneThickness.TIP && !state.getValue(WATERLOGGED);
    }

    private static boolean isStalactite(BlockState state) {
        return isSpeleothemWithDirection(state, Direction.DOWN);
    }

    private static boolean isStalagmite(BlockState state) {
        return isSpeleothemWithDirection(state, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.isValidSpeleothemPlacement(level, pos, state.getValue(TIP_DIRECTION));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }
        Direction tipDirection = state.getValue(TIP_DIRECTION);
        if (tipDirection == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
            return state;
        }
        if (direction == tipDirection.getOpposite() && !this.canSurvive(state, level, pos)) {
            level.scheduleTick(pos, this, tipDirection == Direction.DOWN ? 2 : 1);
            return state;
        }
        boolean mergeOpposingTips = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
        return state.setValue(THICKNESS, this.calculateSpeleothemThickness(level, pos, tipDirection, mergeOpposingTips));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction defaultTipDirection = context.getNearestLookingVerticalDirection().getOpposite();
        Direction tipDirection = this.calculateTipDirection(level, pos, defaultTipDirection);
        if (tipDirection == null) {
            return null;
        }
        boolean mergeOpposingTips = !context.isSecondaryUseActive();
        DripstoneThickness thickness = this.calculateSpeleothemThickness(level, pos, tipDirection, mergeOpposingTips);
        return this.defaultBlockState()
            .setValue(TIP_DIRECTION, tipDirection)
            .setValue(THICKNESS, thickness)
            .setValue(WATERLOGGED, level.getFluidState(pos).is(Fluids.WATER));
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP && state.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(fallDistance + STALAGMITE_FALL_DISTANCE_OFFSET, STALAGMITE_FALL_DAMAGE_MODIFIER, level.damageSources().stalagmite());
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (isFreeHangingStalactite(state)) {
            float randomValue = random.nextFloat();
            if (!(randomValue > 0.12F)) {
                getFluidAboveStalactite(level, pos, state)
                    .filter(fluidAbove -> randomValue < 0.02F || canFillCauldron(fluidAbove.fluid()))
                    .ifPresent(fluidAbove -> spawnDripParticle(level, pos, state, fluidAbove.fluid(), fluidAbove.pos()));
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isStalagmite(state) && !this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        } else {
            spawnFallingStalactite(state, level, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        transferFluid(state, level, pos, random.nextFloat());
        if (random.nextFloat() < 0.011377778F && isStalactiteStartPos(state, level, pos)) {
            this.growIfPossible(state, level, pos, random);
        }
    }

    private static void transferFluid(BlockState state, ServerLevel level, BlockPos pos, float randomValue) {
        if (!isStalactiteStartPos(state, level, pos)) {
            return;
        }
        Optional<FluidInfo> fluidInfo = getFluidAboveStalactite(level, pos, state);
        if (fluidInfo.isEmpty()) {
            return;
        }
        Fluid fluid = fluidInfo.get().fluid();
        float transferProbability = BlockItemHooks.get().dripstoneDripChance(fluid);
        if (transferProbability < 0.0F || randomValue >= transferProbability) {
            return;
        }
        BlockPos stalactiteTipPos = findTip(state, level, pos, 11, false);
        if (stalactiteTipPos == null) {
            return;
        }
        if (fluidInfo.get().sourceState().is(Blocks.MUD) && fluid == Fluids.WATER) {
            BlockState newState = Blocks.CLAY.defaultBlockState();
            level.setBlockAndUpdate(fluidInfo.get().pos(), newState);
            Block.pushEntitiesUp(fluidInfo.get().sourceState(), newState, level, fluidInfo.get().pos());
            level.gameEvent(GameEvent.BLOCK_CHANGE, fluidInfo.get().pos(), GameEvent.Context.of(newState));
            level.levelEvent(1504, stalactiteTipPos, 0);
        } else {
            BlockPos cauldronPos = findFillableCauldronBelowStalactiteTip(level, stalactiteTipPos, fluid);
            if (cauldronPos != null) {
                level.levelEvent(1504, stalactiteTipPos, 0);
                int fallDistance = stalactiteTipPos.getY() - cauldronPos.getY();
                int delay = 50 + fallDistance;
                BlockState cauldronState = level.getBlockState(cauldronPos);
                level.scheduleTick(cauldronPos, cauldronState.getBlock(), delay);
            }
        }
    }

    public static void spawnDripParticle(Level level, BlockPos stalactiteTipPos, BlockState stalactiteTipState) {
        getFluidAboveStalactite(level, stalactiteTipPos, stalactiteTipState)
            .ifPresent(fluidAbove -> spawnDripParticle(level, stalactiteTipPos, stalactiteTipState, fluidAbove.fluid(), fluidAbove.pos()));
    }

    private static void spawnDripParticle(Level level, BlockPos stalactiteTipPos, BlockState stalactiteTipState, Fluid fluidAbove, BlockPos posAbove) {
        Vec3 offset = stalactiteTipState.getOffset(level, stalactiteTipPos);
        double x = stalactiteTipPos.getX() + 0.5 + offset.x;
        double y = stalactiteTipPos.getY() + STALACTITE_DRIP_START_PIXEL - 0.0625;
        double z = stalactiteTipPos.getZ() + 0.5 + offset.z;
        level.addParticle(getDripParticle(level, fluidAbove), x, y, z, 0.0, 0.0, 0.0);
    }

    private static ParticleOptions getDripParticle(Level level, Fluid fluidAbove) {
        if (!fluidAbove.isSame(Fluids.EMPTY)) {
            ParticleOptions particle = BlockItemHooks.get().dripstoneDripParticle(fluidAbove);
            if (particle != null) {
                return particle;
            }
        }
        return level.dimensionType().ultraWarm() ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
    }

    private static void spawnFallingStalactite(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos fallPos = pos.mutable();
        BlockState fallState = state;
        while (isStalactite(fallState)) {
            FallingBlockEntity entity = FallingBlockEntity.fall(level, fallPos, fallState);
            if (isSpeleothemTip(fallState, true)) {
                int size = Math.max(1 + pos.getY() - fallPos.getY(), 6);
                float damagePerFallDistance = 1.0F * size;
                entity.setHurtsEntities(damagePerFallDistance, 40);
                break;
            }
            fallPos.move(Direction.DOWN);
            fallState = level.getBlockState(fallPos);
        }
    }

    private void growIfPossible(BlockState stalactiteStartState, ServerLevel level, BlockPos stalactiteStartPos, RandomSource random) {
        if (this.canGrow(level, stalactiteStartPos)) {
            BlockPos stalactiteTipPos = findTip(stalactiteStartState, level, stalactiteStartPos, 7, false);
            if (stalactiteTipPos != null) {
                BlockState stalactiteTipState = level.getBlockState(stalactiteTipPos);
                if (isFreeHangingStalactite(stalactiteTipState) && this.canTipGrow(stalactiteTipState, level, stalactiteTipPos)) {
                    if (random.nextBoolean()) {
                        this.grow(level, stalactiteTipPos, Direction.DOWN);
                    } else {
                        this.growStalagmiteBelow(level, stalactiteTipPos);
                    }
                }
            }
        }
    }

    private boolean canGrow(LevelReader level, BlockPos pos) {
        FluidState fluidState = level.getBlockState(pos.above(2)).getFluidState();
        return level.getBlockState(pos.above()).is(this.blockToGrowOn.get()) && fluidState.is(Fluids.WATER) && fluidState.isSource();
    }

    private static boolean isStalactiteStartPos(BlockState state, LevelReader level, BlockPos pos) {
        return isStalactite(state) && !level.getBlockState(pos.above()).is(state.getBlock());
    }

    private static @Nullable BlockPos findTip(BlockState speleothemState, LevelAccessor level, BlockPos speleothemPos, int maxSearchLength, boolean includeMergedTip) {
        if (isSpeleothemTip(speleothemState, includeMergedTip)) {
            return speleothemPos;
        }
        Direction searchDirection = speleothemState.getValue(TIP_DIRECTION);
        BiPredicate<BlockPos, BlockState> pathPredicate = (pathPos, state) -> state.is(speleothemState.getBlock())
            && state.getValue(TIP_DIRECTION) == searchDirection;
        return findBlockVertical(level, speleothemPos, searchDirection.getAxisDirection(), pathPredicate,
            speleothem -> isSpeleothemTip(speleothem, includeMergedTip), maxSearchLength).orElse(null);
    }

    private @Nullable Direction calculateTipDirection(LevelReader level, BlockPos pos, Direction defaultTipDirection) {
        if (this.isValidSpeleothemPlacement(level, pos, defaultTipDirection)) {
            return defaultTipDirection;
        }
        if (!this.isValidSpeleothemPlacement(level, pos, defaultTipDirection.getOpposite())) {
            return null;
        }
        return defaultTipDirection.getOpposite();
    }

    private DripstoneThickness calculateSpeleothemThickness(LevelReader level, BlockPos pos, Direction tipDirection, boolean mergeOpposingTips) {
        Direction baseDirection = tipDirection.getOpposite();
        BlockState inFrontState = level.getBlockState(pos.relative(tipDirection));
        if (isSpeleothemWithDirection(inFrontState, baseDirection) && inFrontState.is(this)) {
            return !mergeOpposingTips && inFrontState.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE
                ? DripstoneThickness.TIP
                : DripstoneThickness.TIP_MERGE;
        }
        if (!isSpeleothemWithDirection(inFrontState, tipDirection)) {
            return DripstoneThickness.TIP;
        }
        DripstoneThickness inFrontThickness = inFrontState.getValue(THICKNESS);
        if (inFrontThickness != DripstoneThickness.TIP && inFrontThickness != DripstoneThickness.TIP_MERGE) {
            BlockState behindState = level.getBlockState(pos.relative(baseDirection));
            return !isSpeleothemWithDirection(behindState, tipDirection) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
        }
        return DripstoneThickness.FRUSTUM;
    }

    private boolean isValidSpeleothemPlacement(LevelReader level, BlockPos pos, Direction tipDirection) {
        BlockPos behindPos = pos.relative(tipDirection.getOpposite());
        BlockState behindState = level.getBlockState(behindPos);
        return behindState.isFaceSturdy(level, behindPos, tipDirection) || isSpeleothemWithDirection(behindState, tipDirection) && behindState.is(this);
    }

    private boolean canTipGrow(BlockState tipState, ServerLevel level, BlockPos tipPos) {
        Direction growDirection = tipState.getValue(TIP_DIRECTION);
        BlockState stateAtGrowPos = level.getBlockState(tipPos.relative(growDirection));
        if (!stateAtGrowPos.getFluidState().isEmpty()) {
            return false;
        }
        return stateAtGrowPos.isAir() || this.isUnmergedTipWithDirection(stateAtGrowPos, growDirection.getOpposite());
    }

    private boolean isUnmergedTipWithDirection(BlockState state, Direction tipDirection) {
        return isSpeleothemTip(state, false) && state.getValue(TIP_DIRECTION) == tipDirection && state.is(this);
    }

    private void grow(ServerLevel level, BlockPos growFromPos, Direction growToDirection) {
        BlockPos targetPos = growFromPos.relative(growToDirection);
        BlockState existingStateAtTargetPos = level.getBlockState(targetPos);
        if (this.isUnmergedTipWithDirection(existingStateAtTargetPos, growToDirection.getOpposite())) {
            this.createMergedTips(existingStateAtTargetPos, level, targetPos);
        } else if (existingStateAtTargetPos.isAir() || existingStateAtTargetPos.is(Blocks.WATER)) {
            this.createSpeleothem(level, targetPos, growToDirection, DripstoneThickness.TIP);
        }
    }

    private void createSpeleothem(LevelAccessor level, BlockPos pos, Direction direction, DripstoneThickness thickness) {
        BlockState state = this.defaultBlockState()
            .setValue(TIP_DIRECTION, direction)
            .setValue(THICKNESS, thickness)
            .setValue(WATERLOGGED, level.getFluidState(pos).is(Fluids.WATER));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private void createMergedTips(BlockState tipState, LevelAccessor level, BlockPos tipPos) {
        BlockPos stalactitePos;
        BlockPos stalagmitePos;
        if (tipState.getValue(TIP_DIRECTION) == Direction.UP) {
            stalagmitePos = tipPos;
            stalactitePos = tipPos.above();
        } else {
            stalactitePos = tipPos;
            stalagmitePos = tipPos.below();
        }
        this.createSpeleothem(level, stalactitePos, Direction.DOWN, DripstoneThickness.TIP_MERGE);
        this.createSpeleothem(level, stalagmitePos, Direction.UP, DripstoneThickness.TIP_MERGE);
    }

    private void growStalagmiteBelow(ServerLevel level, BlockPos posAboveStalagmite) {
        BlockPos.MutableBlockPos pos = posAboveStalagmite.mutable();
        for (int i = 0; i < 10; i++) {
            pos.move(Direction.DOWN);
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) {
                return;
            }
            if (this.isUnmergedTipWithDirection(state, Direction.UP) && this.canTipGrow(state, level, pos)) {
                this.grow(level, pos, Direction.UP);
                return;
            }
            if (this.isValidSpeleothemPlacement(level, pos, Direction.UP) && !level.isWaterAt(pos.below())) {
                this.grow(level, pos.below(), Direction.UP);
                return;
            }
            if (!canDripThrough(level, pos, state)) {
                return;
            }
        }
    }

    public static @Nullable BlockPos findStalactiteTipAboveCauldron(Level level, BlockPos cauldronPos) {
        BiPredicate<BlockPos, BlockState> pathPredicate = (pos, state) -> canDripThrough(level, pos, state);
        return findBlockVertical(level, cauldronPos, Direction.UP.getAxisDirection(), pathPredicate,
            AbyssPointedDripstoneBlock::isFreeHangingStalactite, 11).orElse(null);
    }

    public static Fluid getCauldronFillFluidType(ServerLevel level, BlockPos stalactitePos) {
        return getFluidAboveStalactite(level, stalactitePos, level.getBlockState(stalactitePos))
            .map(FluidInfo::fluid)
            .filter(AbyssPointedDripstoneBlock::canFillCauldron)
            .orElse(Fluids.EMPTY);
    }

    private static @Nullable BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos stalactiteTipPos, Fluid fluid) {
        Predicate<BlockState> cauldronPredicate = state -> state.getBlock() instanceof AbstractCauldronBlock cauldron
            && ((AbstractCauldronBlockInvoker) cauldron).theabyss$canReceiveStalactiteDrip(fluid);
        BiPredicate<BlockPos, BlockState> pathPredicate = (pos, state) -> canDripThrough(level, pos, state);
        return findBlockVertical(level, stalactiteTipPos, Direction.DOWN.getAxisDirection(), pathPredicate, cauldronPredicate, 11).orElse(null);
    }

    private static Optional<FluidInfo> getFluidAboveStalactite(Level level, BlockPos stalactitePos, BlockState stalactiteState) {
        if (!isStalactite(stalactiteState)) {
            return Optional.empty();
        }
        return findRootBlock(level, stalactitePos, stalactiteState, 11).map(rootPos -> {
            BlockPos abovePos = rootPos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            Fluid fluid;
            if (aboveState.is(Blocks.MUD) && !level.dimensionType().ultraWarm()) {
                fluid = Fluids.WATER;
            } else {
                fluid = level.getFluidState(abovePos).getType();
            }
            return new FluidInfo(abovePos, fluid, aboveState);
        });
    }

    private static Optional<BlockPos> findRootBlock(Level level, BlockPos pos, BlockState dripStoneState, int maxSearchLength) {
        Direction tipDirection = dripStoneState.getValue(TIP_DIRECTION);
        BiPredicate<BlockPos, BlockState> pathPredicate = (pathPos, state) -> state.is(dripStoneState.getBlock())
            && state.getValue(TIP_DIRECTION) == tipDirection;
        return findBlockVertical(level, pos, tipDirection.getOpposite().getAxisDirection(), pathPredicate,
            state -> !state.is(dripStoneState.getBlock()), maxSearchLength);
    }

    private static boolean canFillCauldron(Fluid fluidAbove) {
        return BlockItemHooks.get().dripstoneDripChance(fluidAbove) >= 0.0F;
    }

    private static Optional<BlockPos> findBlockVertical(LevelAccessor level, BlockPos pos, Direction.AxisDirection axisDirection,
                                                        BiPredicate<BlockPos, BlockState> pathPredicate, Predicate<BlockState> targetPredicate,
                                                        int maxSteps) {
        Direction direction = Direction.get(axisDirection, Direction.Axis.Y);
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        for (int i = 1; i < maxSteps; i++) {
            mutablePos.move(direction);
            BlockState state = level.getBlockState(mutablePos);
            if (targetPredicate.test(state)) {
                return Optional.of(mutablePos.immutable());
            }
            if (level.isOutsideBuildHeight(mutablePos.getY()) || !pathPredicate.test(mutablePos, state)) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean canDripThrough(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.isSolidRender(level, pos)) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        VoxelShape collisionShape = state.getCollisionShape(level, pos);
        return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, collisionShape, BooleanOp.AND);
    }

    private record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
    }
}
