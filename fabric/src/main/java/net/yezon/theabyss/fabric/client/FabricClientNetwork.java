package net.yezon.theabyss.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.core.FabricAttachments;
import net.yezon.theabyss.fabric.core.FabricNetwork;
import net.yezon.theabyss.fabric.core.FabricPlatform;

public final class FabricClientNetwork {
    private FabricClientNetwork() {
    }

    public static void register() {
        FabricPlatform.setClientSender(ClientPlayNetworking::send);
        for (FabricNetwork.ClientReceiver<?> receiver : FabricNetwork.clientReceivers()) {
            register(receiver);
        }
        ClientPlayNetworking.registerGlobalReceiver(FabricAttachments.SyncPayload.TYPE, (payload, context) -> {
            if (context.client().level == null) {
                return;
            }
            Entity entity = context.client().level.getEntity(payload.entityId());
            if (entity != null) {
                FabricAttachments.receive(entity, payload.attachment(), payload.data(), context.client().level.registryAccess());
            }
        });
    }

    private static <T extends CustomPacketPayload> void register(FabricNetwork.ClientReceiver<T> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(receiver.type(), (payload, context) -> {
            Minecraft client = context.client();
            receiver.handler().handle(payload, new FabricNetwork.Context(context.player(), task -> client.submit(task)));
        });
    }
}
