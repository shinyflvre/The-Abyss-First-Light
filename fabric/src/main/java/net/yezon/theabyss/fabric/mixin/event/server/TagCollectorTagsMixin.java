package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.client.multiplayer.TagCollector;
import net.minecraft.core.RegistryAccess;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TagCollector.class)
public abstract class TagCollectorTagsMixin {
    @Inject(method = "updateTags(Lnet/minecraft/core/RegistryAccess;Z)V", at = @At("TAIL"))
    private void theabyss$tagsUpdated(RegistryAccess registries, boolean memoryConnection, CallbackInfo ci) {
        ServerFabricEvents.tagsUpdated();
    }
}
