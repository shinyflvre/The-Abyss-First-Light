package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.hooks.PlantSoilBlock;
import net.yezon.theabyss.compat.TriState;

public class LushClayBlock extends Block implements PlantSoilBlock {
    public LushClayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public TriState sustainsPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        if (facing == Direction.UP && (plant.getBlock() instanceof AzaleaBlock || plant.getBlock() instanceof MangrovePropaguleBlock)) {
            return TriState.TRUE;
        }
        return TriState.DEFAULT;
    }
}
