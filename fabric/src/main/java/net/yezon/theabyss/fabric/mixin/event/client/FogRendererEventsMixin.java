package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererEventsMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1))
    private static void theabyss$fogColor(float red, float green, float blue, float alpha, Operation<Void> original, @Local(argsOnly = true) Camera camera,
        @Local(argsOnly = true, ordinal = 0) float partialTick) {
        if (ClientFabricEvents.listensToFogColor()) {
            Vector3f color = ClientFabricEvents.fogColor(camera, partialTick, fogRed, fogGreen, fogBlue);
            fogRed = color.x();
            fogGreen = color.y();
            fogBlue = color.z();
        }
        original.call(fogRed, fogGreen, fogBlue, alpha);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void theabyss$renderFog(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean foggy, float partialTick, CallbackInfo ci) {
        ClientFabricEvents.renderFog(mode, camera, partialTick);
    }
}
