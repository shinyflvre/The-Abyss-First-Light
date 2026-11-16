package net.yezon.theabyss.fabric.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.hooks.PlantSoilBlock;

public final class FabricSoil {
    private FabricSoil() {
    }

    public static TriState decision(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return soil.getBlock() instanceof PlantSoilBlock block ? block.canSustainPlant(soil, level, soilPosition, facing, plant) : TriState.DEFAULT;
    }

    public static TriState below(BlockGetter level, BlockPos plantPos, BlockState plant) {
        BlockPos soilPos = plantPos.below();
        return decision(level.getBlockState(soilPos), level, soilPos, Direction.UP, plant);
    }

    public static boolean decide(TriState decision, boolean fallback) {
        return decision == TriState.DEFAULT ? fallback : decision == TriState.TRUE;
    }
}
