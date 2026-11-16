package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LushAzaleaBlock extends AzaleaBlock {
    public LushAzaleaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        int minHeight = LushCave.AZALEA.getMinimumHeight(serverLevel).orElse(0);
        return level.isInsideBuildHeight(pos.above(minHeight + 2)) && level.getFluidState(pos.above()).isEmpty();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        LushCave.AZALEA.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
    }
}
