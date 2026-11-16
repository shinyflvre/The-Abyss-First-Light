package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class LushBigDripleafBlock extends BigDripleafBlock {
    public LushBigDripleafBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static void placeWithRandomHeightLush(LevelAccessor level, RandomSource random, BlockPos stemBottomPos, Direction facing) {
        int desiredHeight = Mth.nextInt(random, 2, 5);
        BlockPos.MutableBlockPos pos = stemBottomPos.mutable();
        int height = 0;
        while (height < desiredHeight && canGrowIntoLush(level, pos)) {
            height++;
            pos.move(Direction.UP);
        }
        int leafY = stemBottomPos.getY() + height - 1;
        pos.setY(stemBottomPos.getY());
        while (pos.getY() < leafY) {
            LushBigDripleafStemBlock.placeStem(level, pos, level.getFluidState(pos), facing);
            pos.move(Direction.UP);
        }
        placeLeaf(level, pos, level.getFluidState(pos), facing);
    }

    static boolean canGrowIntoLush(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.isInsideBuildHeight(pos) && (state.isAir() || state.is(Blocks.WATER) || state.is(TheabyssModBlocks.LUSH_SMALL_DRIPLEAF.get()));
    }

    static boolean placeLeaf(LevelAccessor level, BlockPos pos, FluidState fluidState, Direction facing) {
        BlockState leaf = TheabyssModBlocks.LUSH_BIG_DRIPLEAF.get().defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, fluidState.isSourceOfType(Fluids.WATER))
            .setValue(FACING, facing);
        return level.setBlockAndUpdate(pos, leaf);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        var soil = below.canSustainPlant(level, belowPos, Direction.UP, state);
        if (!soil.isDefault()) {
            return soil.isTrue();
        }
        return below.is(this) || below.is(TheabyssModBlocks.LUSH_BIG_DRIPLEAF_STEM.get()) || below.is(BlockTags.SUPPORTS_BIG_DRIPLEAF);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (directionToNeighbour == Direction.UP && neighbourState.is(this)) {
            return TheabyssModBlocks.LUSH_BIG_DRIPLEAF_STEM.get().withPropertiesOf(state);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return canGrowIntoLush(level, pos.above());
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos abovePos = pos.above();
        if (canGrowIntoLush(level, abovePos)) {
            Direction facing = state.getValue(FACING);
            LushBigDripleafStemBlock.placeStem(level, pos, state.getFluidState(), facing);
            placeLeaf(level, abovePos, level.getBlockState(abovePos).getFluidState(), facing);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        boolean dripleafBelow = below.is(this) || below.is(TheabyssModBlocks.LUSH_BIG_DRIPLEAF_STEM.get());
        return this.defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, fluid.isSourceOfType(Fluids.WATER))
            .setValue(FACING, dripleafBelow ? below.getValue(FACING) : context.getHorizontalDirection().getOpposite());
    }
}
