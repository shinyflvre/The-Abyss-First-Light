package net.yezon.theabyss.fabric.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MushroomBlock.class)
public abstract class MushroomBlockSoilMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void theabyss$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return;
        }
        TriState soil = FabricSoil.decision(below, level, belowPos, Direction.UP, state);
        if (soil != TriState.DEFAULT) {
            cir.setReturnValue(soil == TriState.TRUE);
        }
    }
}
