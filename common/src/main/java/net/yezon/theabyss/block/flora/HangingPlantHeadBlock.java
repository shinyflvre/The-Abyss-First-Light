package net.yezon.theabyss.block.flora;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class HangingPlantHeadBlock extends GrowingPlantHeadBlock {
    private static final VoxelShape HANGING = Block.column(8.0, 9.0, 16.0);
    private static final VoxelShape STANDING = Block.column(8.0, 0.0, 15.0);
    private final Supplier<? extends Block> body;

    public HangingPlantHeadBlock(BlockBehaviour.Properties properties, Supplier<? extends Block> body) {
        this(properties, body, Direction.DOWN);
    }

    public HangingPlantHeadBlock(BlockBehaviour.Properties properties, Supplier<? extends Block> body, Direction growth) {
        super(properties, growth, growth == Direction.UP ? STANDING : HANGING, false, NetherVines.GROW_PER_TICK_PROBABILITY);
        this.body = body;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override
    protected Block getBodyBlock() {
        return this.body.get();
    }

    public Block body() {
        return this.body.get();
    }

    public Direction growth() {
        return this.growthDirection;
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return NetherVines.isValidGrowthState(state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.getBlock() instanceof HangingPlantBodyBlock ? HangingPlantBodyBlock.withTop(state, context.getLevel(), context.getClickedPos()) : state;
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess ticks,
        BlockPos pos,
        Direction directionToNeighbour,
        BlockPos neighbourPos,
        BlockState neighbourState,
        RandomSource random
    ) {
        BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        return updated.getBlock() instanceof HangingPlantBodyBlock ? HangingPlantBodyBlock.withTop(updated, level, pos) : updated;
    }
}
