package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.yezon.theabyss.client.gravity.GravityClient;
import net.yezon.theabyss.platform.client.EntityRenderSuppression;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherSuppressionMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void theabyss$resetSuppression(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        EntityRenderSuppression.clear();
        GravityClient.clearShadow();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;Lorg/joml/Quaternionf;)V"))
    private void theabyss$hideFire(EntityRenderDispatcher dispatcher, PoseStack poseStack, MultiBufferSource buffers, Entity entity, Quaternionf rotation,
        Operation<Void> original) {
        if (!EntityRenderSuppression.isHidden(entity)) {
            original.call(dispatcher, poseStack, buffers, entity, rotation);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"))
    private void theabyss$hideShadow(PoseStack poseStack, MultiBufferSource buffers, Entity entity, float strength, float partialTicks, LevelReader level,
        float radius, Operation<Void> original) {
        if (!EntityRenderSuppression.isHidden(entity) && !GravityClient.isShadowHidden(entity)) {
            original.call(poseStack, buffers, entity, strength, partialTicks, level, radius);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void theabyss$endSuppression(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        EntityRenderSuppression.clear();
        GravityClient.clearShadow();
    }
}
