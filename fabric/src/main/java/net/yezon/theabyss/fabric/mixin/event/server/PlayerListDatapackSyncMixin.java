package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListDatapackSyncMixin {
    @Inject(method = "placeNewPlayer", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket"))
    private void theabyss$datapackSyncJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        ServerFabricEvents.datapackSync((PlayerList)(Object)this, player);
    }

    @Inject(method = "reloadResources", at = @At(value = "NEW", target = "net/minecraft/network/protocol/common/ClientboundUpdateTagsPacket"))
    private void theabyss$datapackSyncReload(CallbackInfo ci) {
        ServerFabricEvents.datapackSync((PlayerList)(Object)this, null);
    }
}
