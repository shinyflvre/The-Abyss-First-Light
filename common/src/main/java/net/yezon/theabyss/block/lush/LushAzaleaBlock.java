package net.yezon.theabyss.block.lush;

import net.yezon.theabyss.init.TheabyssModBlocks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LushAzaleaBlock extends AzaleaBlock {
    public LushAzaleaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        int minHeight = LushCave.azaleaMinimumHeight(serverLevel).orElse(0);
        return !level.isOutsideBuildHeight(pos.above(minHeight + 2)) && level.getFluidState(pos.above()).isEmpty();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        LushCave.AZALEA.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TheabyssModBlocks.LUSH_CLAY.get()) || super.mayPlaceOn(state, level, pos);
    }
}
