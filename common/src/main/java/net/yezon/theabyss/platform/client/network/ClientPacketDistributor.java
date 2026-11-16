package net.yezon.theabyss.platform.client.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.yezon.theabyss.platform.Platform;

public final class ClientPacketDistributor {
    private ClientPacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToServer(payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToServer(extra);
        }
    }
}
