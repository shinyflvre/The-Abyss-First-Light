package net.yezon.theabyss.fabric.mixin.fluids.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerFluidMixin {
    @ModifyExpressionValue(
        method = "aiStep",
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 0),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 1),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z", ordinal = 2)
        }
    )
    private boolean theabyss$startSwimmingInOwnFluids(boolean original) {
        return original || FluidContact.canStartSwimming((LocalPlayer)(Object)this);
    }

    @ModifyExpressionValue(
        method = "aiStep",
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 0),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 1),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 2),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z", ordinal = 3)
        }
    )
    private boolean theabyss$swimSprintInOwnFluids(boolean original) {
        return original || FluidContact.present((LocalPlayer)(Object)this, FluidType::canSwim);
    }
}
