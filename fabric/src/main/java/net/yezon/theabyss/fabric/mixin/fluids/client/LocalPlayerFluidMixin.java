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
        method = "shouldStopSwimSprinting",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z")
    )
    private boolean theabyss$keepSwimSprintInOwnFluids(boolean original) {
        return original || FluidContact.height((LocalPlayer)(Object)this, FluidType::canSwim) > 0.0;
    }
}
