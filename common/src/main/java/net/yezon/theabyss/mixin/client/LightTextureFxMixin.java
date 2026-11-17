package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LightTexture;
import net.yezon.theabyss.client.fx.AbyssBrightness;
import net.yezon.theabyss.client.fx.FearClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class LightTextureFxMixin {
    @Shadow
    private boolean updateLightTexture;

    @Inject(method = "updateLightTexture", at = @At("HEAD"))
    private void theabyss$fearFlickerUpdate(float partialTick, CallbackInfo ci) {
        if (FearClient.lightFlicker()) {
            this.updateLightTexture = true;
        }
    }

    @ModifyExpressionValue(method = "updateLightTexture", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F"))
    private float theabyss$fearBlockFactor(float flicker) {
        return FearClient.blockFactor(flicker + 1.5F) - 1.5F;
    }

    @WrapOperation(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2))
    private float theabyss$abyssBrightness(float a, float b, Operation<Float> original) {
        return AbyssBrightness.brightness(original.call(a, b));
    }
}
