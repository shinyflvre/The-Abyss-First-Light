package net.yezon.theabyss.fabric.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CactusBlock.class)
public abstract class CactusBlockSoilMixin {
    @Inject(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void theabyss$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        TriState soil = FabricSoil.below(level, pos, state);
        if (soil != TriState.DEFAULT) {
            cir.setReturnValue(soil == TriState.TRUE);
        }
    }
}
