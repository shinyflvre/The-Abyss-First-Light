package net.yezon.theabyss.fabric.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmallDripleafBlock.class)
public abstract class SmallDripleafBlockSoilMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void theabyss$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            return;
        }
        TriState soil = FabricSoil.below(level, pos, state);
        if (soil != TriState.DEFAULT) {
            cir.setReturnValue(soil == TriState.TRUE);
        }
    }
}
