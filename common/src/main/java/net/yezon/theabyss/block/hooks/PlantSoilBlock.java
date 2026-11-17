package net.yezon.theabyss.block.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface PlantSoilBlock {
    TriState sustainsPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant);
}
