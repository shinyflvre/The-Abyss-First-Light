package net.yezon.theabyss.fabric.mixin.entity;

import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.fabric.entity.FabricPersistentData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerPersistentDataMixin {
    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("HEAD"))
    private void theabyss$copyPlayerPersisted(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        FabricPersistentData.copyPlayerPersisted(oldPlayer, (ServerPlayer)(Object)this);
    }
}
