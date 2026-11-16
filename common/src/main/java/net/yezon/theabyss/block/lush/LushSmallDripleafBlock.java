package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class LushSmallDripleafBlock extends SmallDripleafBlock {
    public LushSmallDripleafBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            BlockPos above = pos.above();
            level.setBlock(above, level.getFluidState(above).createLegacyBlock(), 18);
            LushBigDripleafBlock.placeWithRandomHeightLush(level, random, pos, state.getValue(FACING));
        } else {
            BlockPos below = pos.below();
            this.performBonemeal(level, random, below, level.getBlockState(below), source);
        }
    }
}
