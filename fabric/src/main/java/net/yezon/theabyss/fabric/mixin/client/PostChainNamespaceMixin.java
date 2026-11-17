package net.yezon.theabyss.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.fabric.client.FabricNamespacedPaths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostChain.class)
public abstract class PostChainNamespaceMixin {
    @WrapOperation(method = "parsePassNode",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation theabyss$namespacedTexture(String path, Operation<ResourceLocation> original) {
        ResourceLocation namespaced = FabricNamespacedPaths.resolve("textures/effect/", path);
        return namespaced != null ? namespaced : original.call(path);
    }
}
