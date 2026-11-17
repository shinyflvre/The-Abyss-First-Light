package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneBlockFluidMixin {
    @WrapOperation(
        method = "canSurvive",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$ownFluidsFeedSugarCane(FluidState fluid, TagKey<Fluid> tag, Operation<Boolean> original) {
        return original.call(fluid, tag) || tag == FluidTags.WATER && FluidContact.hydrates(fluid);
    }
}
