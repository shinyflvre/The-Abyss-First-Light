package net.yezon.theabyss.block.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface ClimbableBlock {
    boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity);
}
