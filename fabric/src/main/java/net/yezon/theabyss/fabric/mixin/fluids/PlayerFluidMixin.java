package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerFluidMixin {
    @ModifyExpressionValue(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$noTurtleBreathInOwnFluids(boolean original) {
        return original || FluidContact.eyeIn((Player)(Object)this, FluidType::canDrown);
    }

    @ModifyExpressionValue(
        method = "updateIsUnderwater",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$underOwnFluids(boolean original) {
        return original || FluidContact.eyeIn((Player)(Object)this, FluidType::canSwim);
    }
}
