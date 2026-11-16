package net.yezon.theabyss.fabric.mixin.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorFluidMixin {
    @Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
    private static void theabyss$ownFluidPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
        FluidType type = FluidContact.typeOf(level.getFluidState(pos));
        if (type != null && type.getPathType() != null) {
            cir.setReturnValue(type.getPathType());
        }
    }
}
