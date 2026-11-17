package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.yezon.theabyss.client.fx.PostFx;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererPostFxMixin {
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getProjectionMatrix(D)Lorg/joml/Matrix4f;", ordinal = 0))
    private Matrix4f theabyss$captureProjection(GameRenderer renderer, double fov, Operation<Matrix4f> original) {
        Matrix4f projection = original.call(renderer, fov);
        PostFx.captureProjection(projection);
        return projection;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    private void theabyss$saveDepth(DeltaTracker deltaTracker, CallbackInfo ci) {
        PostFx.saveDepth();
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void theabyss$captureHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        PostFx.captureHand();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void theabyss$applyPostEffects(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        PostFx.apply(deltaTracker);
    }

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void theabyss$reloadPostEffects(ResourceProvider provider, CallbackInfo ci) {
        PostFx.reload();
    }
}
