package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.fabric.mixin.client.LevelRendererAccessor;
import net.yezon.theabyss.platform.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererEventsMixin {
    @Shadow
    private boolean panoramicMode;

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double theabyss$computeFov(double fov, @Local(argsOnly = true) Camera camera, @Local(argsOnly = true) float partialTick,
        @Local(argsOnly = true) boolean useFovSetting) {
        if (this.panoramicMode) {
            return fov;
        }
        return ClientFabricEvents.fov(camera, partialTick, fov, useFovSetting);
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    private void theabyss$afterLevel(LevelRenderer levelRenderer, DeltaTracker deltaTracker, boolean renderOutline, Camera camera, GameRenderer gameRenderer,
        LightTexture lightTexture, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Operation<Void> original) {
        original.call(levelRenderer, deltaTracker, renderOutline, camera, gameRenderer, lightTexture, modelViewMatrix, projectionMatrix);
        if (ClientFabricEvents.listensToLevelStages()) {
            LevelRendererAccessor accessor = (LevelRendererAccessor)levelRenderer;
            ClientFabricEvents.levelStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, levelRenderer, null, modelViewMatrix, projectionMatrix, accessor.theabyss$ticks(),
                camera, accessor.theabyss$capturedFrustum() != null ? accessor.theabyss$capturedFrustum() : accessor.theabyss$cullingFrustum());
        }
    }
}
