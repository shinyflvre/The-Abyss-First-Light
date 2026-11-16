package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

public class NightBladeIceWalkEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.getBlockState(BlockPos.containing(x, y - 1.0, z)).getBlock() instanceof LiquidBlock) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.setBlock(BlockPos.containing(x + dx, y - 1.0, z + dz), Blocks.ICE.defaultBlockState(), 3);
                }
            }
        }
    }
}
