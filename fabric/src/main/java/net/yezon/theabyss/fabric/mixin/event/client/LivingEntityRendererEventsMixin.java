package net.yezon.theabyss.fabric.mixin.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererEventsMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"), cancellable = true)
    private void theabyss$renderLivingPre(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
        CallbackInfo ci) {
        if (ClientFabricEvents.renderLivingPre(entity, (LivingEntityRenderer<?, ?>)(Object)this, partialTick, poseStack, buffers, packedLight)) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("TAIL"))
    private void theabyss$renderLivingPost(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
        CallbackInfo ci) {
        ClientFabricEvents.renderLivingPost(entity, (LivingEntityRenderer<?, ?>)(Object)this, partialTick, poseStack, buffers, packedLight);
    }
}
