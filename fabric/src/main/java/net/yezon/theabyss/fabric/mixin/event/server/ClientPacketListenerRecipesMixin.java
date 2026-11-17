package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.yezon.theabyss.fabric.event.ServerFabricEventsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerRecipesMixin {
    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void theabyss$recipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        ServerFabricEventsClient.recipesUpdated(((ClientPacketListener)(Object)this).getRecipeManager());
    }
}
