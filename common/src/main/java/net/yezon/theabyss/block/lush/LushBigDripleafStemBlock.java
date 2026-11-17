package net.yezon.theabyss.block.lush;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.BlockUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class LushBigDripleafStemBlock extends BigDripleafStemBlock {
    public LushBigDripleafStemBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    static boolean placeStem(LevelAccessor level, BlockPos pos, FluidState fluidState, Direction facing) {
        BlockState stem = TheabyssModBlocks.LUSH_BIG_DRIPLEAF_STEM.get().defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, fluidState.isSourceOfType(Fluids.WATER))
            .setValue(FACING, facing);
        return level.setBlock(pos, stem, 3);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());
        return (below.is(this) || below.is(BlockTags.BIG_DRIPLEAF_PLACEABLE)) && (above.is(this) || above.is(TheabyssModBlocks.LUSH_BIG_DRIPLEAF.get()));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        Optional<BlockPos> head = BlockUtil.getTopConnectedBlock(level, pos, state.getBlock(), Direction.UP, TheabyssModBlocks.LUSH_BIG_DRIPLEAF.get());
        return head.filter(p -> LushBigDripleafBlock.canGrowIntoLush(level, p.above())).isPresent();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Optional<BlockPos> head = BlockUtil.getTopConnectedBlock(level, pos, state.getBlock(), Direction.UP, TheabyssModBlocks.LUSH_BIG_DRIPLEAF.get());
        if (head.isPresent()) {
            BlockPos headPos = head.get();
            BlockPos newHead = headPos.above();
            Direction facing = state.getValue(FACING);
            placeStem(level, headPos, level.getFluidState(headPos), facing);
            LushBigDripleafBlock.placeLeaf(level, newHead, level.getFluidState(newHead), facing);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(TheabyssModBlocks.LUSH_BIG_DRIPLEAF.get());
    }
}
