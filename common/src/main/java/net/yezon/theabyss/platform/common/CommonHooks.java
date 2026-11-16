package net.yezon.theabyss.platform.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.services.BlockItemHooks;

public final class CommonHooks {
    private CommonHooks() {
    }

    public static boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
        return BlockItemHooks.get().canCropGrow(level, pos, state, def);
    }

    public static void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
        BlockItemHooks.get().fireCropGrowPost(level, pos, state);
    }
}
