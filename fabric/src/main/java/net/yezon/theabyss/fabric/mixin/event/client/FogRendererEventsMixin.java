package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererEventsMixin {
    @Shadow
    @Final
    private static List<FogEnvironment> FOG_ENVIRONMENTS;

    @Inject(method = "computeFogColor", at = @At("TAIL"))
    private void theabyss$fogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f dest,
        CallbackInfo ci) {
        ClientFabricEvents.fogColor(dest);
    }

    @ModifyReturnValue(method = "setupFog", at = @At("RETURN"))
    private FogData theabyss$renderFog(FogData fog, @Local(argsOnly = true) Camera camera, @Local FogType fogType) {
        if (!ClientFabricEvents.listensToRenderFog()) {
            return fog;
        }
        Entity entity = camera.entity();
        FogEnvironment chosen = null;
        for (FogEnvironment environment : FOG_ENVIRONMENTS) {
            if (environment.isApplicable(fogType, entity)) {
                chosen = environment;
                break;
            }
        }
        ClientFabricEvents.renderFog(chosen, fog);
        return fog;
    }
}
