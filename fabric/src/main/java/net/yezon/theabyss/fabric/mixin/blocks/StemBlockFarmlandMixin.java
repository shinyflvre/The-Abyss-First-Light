package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StemBlock.class)
public abstract class StemBlockFarmlandMixin {
    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    private boolean theabyss$modFarmland(boolean original, @Local(argsOnly = true) BlockState soil) {
        return original || FabricSoil.isAbyssFarmland(soil);
    }
}
