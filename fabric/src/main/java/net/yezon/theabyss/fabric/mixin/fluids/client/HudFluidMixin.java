package net.yezon.theabyss.fabric.mixin.fluids.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class HudFluidMixin {
    @ModifyExpressionValue(
        method = "extractAirBubbles",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$bubblesInOwnFluids(boolean original, @Local(argsOnly = true) Player player) {
        return original || FluidContact.eyeIn(player, FluidType::canDrown);
    }
}
