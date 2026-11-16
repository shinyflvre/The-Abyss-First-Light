package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record EchoVisionMessage(boolean start, Vec3 fromEye, float yaw, float pitch, Vec3 body, float range) implements CustomPacketPayload {
    public static final Type<EchoVisionMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "echo_vision"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoVisionMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, EchoVisionMessage::start,
        Vec3.STREAM_CODEC, EchoVisionMessage::fromEye,
        ByteBufCodecs.FLOAT, EchoVisionMessage::yaw,
        ByteBufCodecs.FLOAT, EchoVisionMessage::pitch,
        Vec3.STREAM_CODEC, EchoVisionMessage::body,
        ByteBufCodecs.FLOAT, EchoVisionMessage::range,
        EchoVisionMessage::new
    );

    @Override
    public Type<EchoVisionMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, boolean start, Vec3 fromEye, float yaw, float pitch, Vec3 body, float range) {
        if (player.connection != null && player.connection.hasChannel(TYPE)) {
            PacketDistributor.sendToPlayer(player, new EchoVisionMessage(start, fromEye, yaw, pitch, body, range));
        }
    }

    public static void handleData(EchoVisionMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(EchoVisionMessage m) {
            net.yezon.theabyss.client.fx.EchoVisionClient.receive(m.start(), m.fromEye(), m.yaw(), m.pitch(), m.body(), m.range());
        }
    }
}
