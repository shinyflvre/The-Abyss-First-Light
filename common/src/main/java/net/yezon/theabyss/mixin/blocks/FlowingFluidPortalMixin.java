package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.yezon.theabyss.block.TheAbyssDimPortalBlock;
import net.yezon.theabyss.block.TheabyssdimiceworldPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidPortalMixin {
    @ModifyReturnValue(method = "canHoldFluid", at = @At("RETURN"))
    private boolean theabyss$keepPortals(boolean original, @Local(argsOnly = true) BlockState state) {
        if (original && (state.getBlock() instanceof TheAbyssDimPortalBlock || state.getBlock() instanceof TheabyssdimiceworldPortalBlock)) {
            return false;
        }
        return original;
    }
}
