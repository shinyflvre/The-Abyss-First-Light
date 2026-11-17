package net.yezon.theabyss.fabric.mixin.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.platform.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererEventsMixin {
    @Shadow
    private int ticks;

    @Shadow
    private Frustum cullingFrustum;

    @Shadow
    private @Nullable Frustum capturedFrustum;

    @Unique
    private void theabyss$stage(RenderLevelStageEvent.Stage stage, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (!ClientFabricEvents.listensToLevelStages()) {
            return;
        }
        Frustum frustum = this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum;
        ClientFabricEvents.levelStage(stage, (LevelRenderer)(Object)this, poseStack, modelViewMatrix, projectionMatrix, this.ticks,
            Minecraft.getInstance().gameRenderer.getMainCamera(), frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        shift = At.Shift.AFTER))
    private void theabyss$afterSky(DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        this.theabyss$stage(RenderLevelStageEvent.Stage.AFTER_SKY, null, modelViewMatrix, projectionMatrix);
    }

    @Inject(method = "renderSectionLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V"))
    private void theabyss$afterLayer(RenderType renderType, double x, double y, double z, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        RenderLevelStageEvent.Stage stage;
        if (renderType == RenderType.solid()) {
            stage = RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS;
        } else if (renderType == RenderType.cutoutMipped()) {
            stage = RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS;
        } else if (renderType == RenderType.cutout()) {
            stage = RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS;
        } else if (renderType == RenderType.translucent()) {
            stage = RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
        } else if (renderType == RenderType.tripwire()) {
            stage = RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS;
        } else {
            return;
        }
        this.theabyss$stage(stage, null, modelViewMatrix, projectionMatrix);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
        args = "ldc=blockentities"))
    private void theabyss$afterEntities(DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStack) {
        this.theabyss$stage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, poseStack, modelViewMatrix, projectionMatrix);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
        args = "ldc=destroyProgress"))
    private void theabyss$afterBlockEntities(DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStack) {
        this.theabyss$stage(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, poseStack, modelViewMatrix, projectionMatrix);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
        shift = At.Shift.AFTER))
    private void theabyss$afterParticles(DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStack) {
        this.theabyss$stage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, poseStack, modelViewMatrix, projectionMatrix);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.AFTER))
    private void theabyss$afterWeather(DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStack) {
        this.theabyss$stage(RenderLevelStageEvent.Stage.AFTER_WEATHER, poseStack, modelViewMatrix, projectionMatrix);
    }
}
