package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.server.ReloadableServerResources;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesTagsMixin {
    @Inject(method = "updateRegistryTags()V", at = @At("TAIL"))
    private void theabyss$tagsUpdated(CallbackInfo ci) {
        ServerFabricEvents.tagsUpdated();
    }
}
