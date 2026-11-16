package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.RecipeType;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListDatapackSyncMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void theabyss$datapackSyncJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        ServerFabricEvents.sendRecipes(player, ServerFabricEvents.datapackSync(), this.server);
    }

    @Inject(method = "reloadResources", at = @At(value = "NEW", target = "net/minecraft/network/protocol/common/ClientboundUpdateTagsPacket"))
    private void theabyss$datapackSyncReload(CallbackInfo ci, @Share("theabyss$recipeTypes") LocalRef<Set<RecipeType<?>>> types) {
        types.set(ServerFabricEvents.datapackSync());
    }

    @Inject(method = "reloadResources", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/stats/ServerRecipeBook;sendInitialRecipeBook(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void theabyss$datapackSyncReloadSend(CallbackInfo ci, @Local ServerPlayer player, @Share("theabyss$recipeTypes") LocalRef<Set<RecipeType<?>>> types) {
        ServerFabricEvents.sendRecipes(player, types.get(), this.server);
    }
}
