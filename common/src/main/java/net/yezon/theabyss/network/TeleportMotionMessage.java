package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.platform.services.ServerHooks;
import net.yezon.theabyss.util.AbyssTeleport;

public record TeleportMotionMessage(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<TeleportMotionMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "teleport_motion"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportMotionMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, TeleportMotionMessage::x,
        ByteBufCodecs.DOUBLE, TeleportMotionMessage::y,
        ByteBufCodecs.DOUBLE, TeleportMotionMessage::z,
        TeleportMotionMessage::new
    );

    @Override
    public Type<TeleportMotionMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, double x, double y, double z) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new TeleportMotionMessage(x, y, z));
        }
    }

    public static void handleData(TeleportMotionMessage message, IPayloadContext context) {
        AbyssTeleport.expectKeptMotion(message.x(), message.y(), message.z());
    }
}
