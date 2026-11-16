package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationTagsMixin {
    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V",
        shift = At.Shift.AFTER))
    private void theabyss$tagsUpdated(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
        ServerFabricEvents.tagsUpdated();
    }
}
