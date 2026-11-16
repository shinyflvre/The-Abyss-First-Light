package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelJoinMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void theabyss$joinLevel(Entity entity, CallbackInfo ci) {
        if (EntityFabricEvents.joinLevel(entity, (ClientLevel)(Object)this, false)) {
            ci.cancel();
        }
    }
}
