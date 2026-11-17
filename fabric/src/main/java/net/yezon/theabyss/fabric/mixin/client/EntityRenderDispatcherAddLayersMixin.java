package net.yezon.theabyss.fabric.mixin.client;

import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.client.FabricRenderRegistrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherAddLayersMixin {
    @Shadow
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void theabyss$addLayers(ResourceManager resourceManager, CallbackInfo ci) {
        FabricRenderRegistrations.addLayers(this.playerRenderers);
    }
}
