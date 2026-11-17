package net.yezon.theabyss.fabric.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MangrovePropaguleBlock.class)
public abstract class MangrovePropaguleBlockSoilMixin {
    @Shadow
    private static boolean isHanging(BlockState state) {
        throw new AssertionError();
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void theabyss$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!isHanging(state)) {
            return;
        }
        BlockPos soilPos = pos.above();
        TriState soil = FabricSoil.decision(level.getBlockState(soilPos), level, soilPos, Direction.DOWN, state);
        if (soil != TriState.DEFAULT) {
            cir.setReturnValue(soil == TriState.TRUE);
        }
    }
}
