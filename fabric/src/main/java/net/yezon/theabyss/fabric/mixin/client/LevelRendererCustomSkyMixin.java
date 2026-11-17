package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.yezon.theabyss.client.fx.CustomSky;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererCustomSkyMixin {
    @Shadow
    private ClientLevel level;

    @Shadow
    private int ticks;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void theabyss$customSky(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable setupFog,
        CallbackInfo ci) {
        if (this.level != null && this.level.effects() instanceof CustomSky sky
            && sky.renderSky(this.level, this.ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog)) {
            ci.cancel();
        }
    }
}
