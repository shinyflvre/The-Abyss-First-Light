package net.yezon.theabyss.fabric.mixin.fluids.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererFluidMixin {
    @ModifyExpressionValue(
        method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInWater()Z")
    )
    private boolean theabyss$swimPoseInOwnFluids(boolean original, @Local(argsOnly = true) AbstractClientPlayer player) {
        return original || FluidContact.present(player, FluidType::canSwim);
    }
}
