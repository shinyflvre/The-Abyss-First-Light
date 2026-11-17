package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneFluidMixin {
    @WrapOperation(
        method = "spawnDripParticle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Fluid;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private static boolean theabyss$waterDripBelowOwnFluids(Fluid fluid, TagKey<Fluid> tag, Operation<Boolean> original) {
        return FluidContact.typeOf(fluid) == null && original.call(fluid, tag);
    }
}
