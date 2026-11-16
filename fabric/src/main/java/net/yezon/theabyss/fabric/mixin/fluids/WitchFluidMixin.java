package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.monster.Witch;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Witch.class)
public abstract class WitchFluidMixin {
    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$breathingPotionInOwnFluids(boolean original) {
        return original || FluidContact.eyeIn((Witch)(Object)this, FluidType::canDrown);
    }
}
