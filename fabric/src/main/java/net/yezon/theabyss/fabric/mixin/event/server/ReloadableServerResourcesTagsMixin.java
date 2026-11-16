package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.server.ReloadableServerResources;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesTagsMixin {
    @Inject(method = "updateComponentsAndStaticRegistryTags",
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/ReloadableServerResources;newComponents:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private void theabyss$tagsUpdated(CallbackInfo ci) {
        ServerFabricEvents.tagsUpdated();
    }
}
