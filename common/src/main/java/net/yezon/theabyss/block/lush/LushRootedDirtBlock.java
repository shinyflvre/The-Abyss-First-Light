package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RootedDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class LushRootedDirtBlock extends RootedDirtBlock {
    public LushRootedDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos.below(), TheabyssModBlocks.LUSH_HANGING_ROOTS.get().defaultBlockState());
    }
}
