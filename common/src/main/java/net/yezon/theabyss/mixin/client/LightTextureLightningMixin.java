package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LightTexture;
import net.yezon.theabyss.weather.client.AbyssLightningClient;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureLightningMixin {
    @WrapOperation(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;"))
    private Vector3f theabyss$lightningTint(Vector3f self, Vector3fc other, float t, Operation<Vector3f> original) {
        if (t == 0.35F && other.x() == 1.0F && other.y() == 1.0F && other.z() == 1.0F) {
            Vector3f sky = original.call(self, other, t);
            net.yezon.theabyss.client.fx.RiftSky.lightmapSky(sky);
            AbyssLightningClient.tintLightmap(sky);
            return sky;
        }
        if (t == 0.04F && other.x() == 0.75F && other.y() == 0.75F && other.z() == 0.75F) {
            Vector3f ambient = new Vector3f(other);
            AbyssLightningClient.tintLightmap(ambient);
            return original.call(self, ambient, t);
        }
        return original.call(self, other, t);
    }
}
