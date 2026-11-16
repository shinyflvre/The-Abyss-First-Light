package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeFluidMixin {
    @ModifyExpressionValue(
        method = "travelInFluid",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/cubemob/SulfurCube;getFluidHeight(Lnet/minecraft/tags/TagKey;)D")
    )
    private double theabyss$floatOnOwnFluids(double original) {
        double own = FluidContact.highestIfOwn((SulfurCube)(Object)this);
        return own > 0.0 ? own : original;
    }
}
