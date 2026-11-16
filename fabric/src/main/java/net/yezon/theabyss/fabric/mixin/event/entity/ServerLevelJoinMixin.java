package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelJoinMixin {
    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void theabyss$playerJoinLevel(ServerPlayer player, CallbackInfo ci) {
        if (EntityFabricEvents.joinLevel(player, (ServerLevel)(Object)this, false)) {
            ci.cancel();
        }
    }
}
