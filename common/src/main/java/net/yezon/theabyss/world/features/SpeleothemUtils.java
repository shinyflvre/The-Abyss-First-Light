package net.yezon.theabyss.world.features;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

final class SpeleothemUtils {
    private SpeleothemUtils() {
    }

    static boolean isEmptyOrWater(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, SpeleothemUtils::isEmptyOrWater);
    }

    static void buildBaseToTipColumn(Direction direction, int totalLength, boolean mergedTip, Consumer<BlockState> consumer, Block pointedBlock) {
        if (totalLength >= 3) {
            consumer.accept(createPointedBlock(direction, DripstoneThickness.BASE, pointedBlock));

            for (int i = 0; i < totalLength - 3; i++) {
                consumer.accept(createPointedBlock(direction, DripstoneThickness.MIDDLE, pointedBlock));
            }
        }

        if (totalLength >= 2) {
            consumer.accept(createPointedBlock(direction, DripstoneThickness.FRUSTUM, pointedBlock));
        }

        if (totalLength >= 1) {
            consumer.accept(createPointedBlock(direction, mergedTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP, pointedBlock));
        }
    }

    static void growSpeleothem(LevelAccessor level, BlockPos startPos, Direction tipDirection, int height, boolean mergedTip, Block baseBlock,
        Block pointedBlock, HolderSet<Block> replaceableBlocks) {
        if (isBase(level.getBlockState(startPos.relative(tipDirection.getOpposite())), baseBlock, replaceableBlocks)) {
            BlockPos.MutableBlockPos pos = startPos.mutable();
            buildBaseToTipColumn(tipDirection, height, mergedTip, state -> {
                if (state.is(pointedBlock)) {
                    state = state.setValue(PointedDripstoneBlock.WATERLOGGED, level.isWaterAt(pos));
                }

                level.setBlock(pos, state, 2);
                pos.move(tipDirection);
            }, pointedBlock);
        }
    }

    static boolean placeBaseBlockIfPossible(LevelAccessor level, BlockPos pos, Block baseBlock, HolderSet<Block> replaceableBlocks) {
        BlockState state = level.getBlockState(pos);
        if (state.is(replaceableBlocks)) {
            level.setBlock(pos, baseBlock.defaultBlockState(), 2);
            return true;
        }
        return false;
    }

    private static BlockState createPointedBlock(Direction direction, DripstoneThickness thickness, Block pointedBlock) {
        return pointedBlock.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction).setValue(PointedDripstoneBlock.THICKNESS, thickness);
    }

    static boolean isBase(BlockState state, Block baseBlock, HolderSet<Block> replaceableBlocks) {
        return state.is(baseBlock) || state.is(replaceableBlocks);
    }

    static boolean isEmptyOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }

    static boolean isNeitherEmptyNorWater(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER);
    }
}
