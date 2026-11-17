package net.yezon.theabyss.fabric.mixin.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererEventsMixin {
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
        cancellable = true)
    private void theabyss$renderPlayerPre(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, CallbackInfo ci) {
        if (ClientFabricEvents.renderPlayerPre(player, (PlayerRenderer)(Object)this, partialTick, poseStack, buffers, packedLight)) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("TAIL"))
    private void theabyss$renderPlayerPost(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, CallbackInfo ci) {
        ClientFabricEvents.renderPlayerPost(player, (PlayerRenderer)(Object)this, partialTick, poseStack, buffers, packedLight);
    }

    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void theabyss$renderRightArm(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        if (ClientFabricEvents.renderArm(poseStack, buffers, packedLight, player, HumanoidArm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void theabyss$renderLeftArm(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        if (ClientFabricEvents.renderArm(poseStack, buffers, packedLight, player, HumanoidArm.LEFT)) {
            ci.cancel();
        }
    }
}
