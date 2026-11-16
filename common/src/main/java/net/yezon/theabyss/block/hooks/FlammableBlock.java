package net.yezon.theabyss.block.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlammableBlock {
    int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face);
}
