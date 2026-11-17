package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.client.gravity.GravityClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererGravityMixin {
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void theabyss$gravityHeadModel(EntityModel<Entity> model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch, Operation<Void> original) {
        if (entity instanceof LivingEntity living) {
            original.call(model, entity, limbSwing, limbSwingAmount, ageInTicks, GravityClient.headYaw(living, netHeadYaw), GravityClient.headPitch(living, headPitch));
        } else {
            original.call(model, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private void theabyss$gravityHeadLayers(RenderLayer<Entity, EntityModel<Entity>> layer, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
        Entity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, Operation<Void> original) {
        if (entity instanceof LivingEntity living) {
            original.call(layer, poseStack, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                GravityClient.headYaw(living, netHeadYaw), GravityClient.headPitch(living, headPitch));
        } else {
            original.call(layer, poseStack, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
