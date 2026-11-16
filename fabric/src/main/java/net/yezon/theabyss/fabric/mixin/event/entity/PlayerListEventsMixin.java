package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListEventsMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void theabyss$loggedIn(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        PlayerFabricEvents.loggedIn(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void theabyss$loggedOut(ServerPlayer player, CallbackInfo ci) {
        PlayerFabricEvents.loggedOut(player);
    }

    @WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V"))
    private void theabyss$respawn(ServerPlayer player, float health, Operation<Void> original) {
        original.call(player, health);
        PlayerFabricEvents.respawn(player);
    }
}
