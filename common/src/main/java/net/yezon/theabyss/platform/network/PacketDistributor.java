package net.yezon.theabyss.platform.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.Platform;

public final class PacketDistributor {
    private PacketDistributor() {
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToPlayer(player, payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToPlayer(player, extra);
        }
    }

    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToPlayersInDimension(level, payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToPlayersInDimension(level, extra);
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToAllPlayers(payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToAllPlayers(extra);
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToPlayersTrackingEntity(entity, payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToPlayersTrackingEntity(entity, extra);
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Platform.get().sendToPlayersTrackingEntityAndSelf(entity, payload);
        for (CustomPacketPayload extra : payloads) {
            Platform.get().sendToPlayersTrackingEntityAndSelf(entity, extra);
        }
    }
}
