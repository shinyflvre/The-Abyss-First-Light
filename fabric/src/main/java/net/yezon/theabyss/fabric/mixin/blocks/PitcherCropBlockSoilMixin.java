package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PitcherCropBlock.class)
public abstract class PitcherCropBlockSoilMixin {
    @Shadow
    private static boolean isLower(BlockState state) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean sufficientLight(LevelReader level, BlockPos pos) {
        throw new AssertionError();
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void theabyss$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (isLower(state) && !sufficientLight(level, pos) && FabricSoil.below(level, pos, state) == TriState.TRUE) {
            cir.setReturnValue(true);
        }
    }

    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    private boolean theabyss$modFarmland(boolean original, @Local(argsOnly = true) BlockState soil) {
        return original || FabricSoil.isAbyssFarmland(soil);
    }
}
