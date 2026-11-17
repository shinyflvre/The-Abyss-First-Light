package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.yezon.theabyss.client.fx.TrueInvisibilityClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({CapeLayer.class, Deadmau5EarsLayer.class})
public abstract class TrueInvisibilityCapeMixin {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z"))
    private boolean theabyss$revealCape(AbstractClientPlayer player, Operation<Boolean> original) {
        return original.call(player) && !TrueInvisibilityClient.revealed(player);
    }
}
