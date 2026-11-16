package net.yezon.theabyss.network;

import java.util.List;
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
import net.yezon.theabyss.client.fx.ClockRewindClient;

public record ClockRewindMessage(List<Sample> recent, List<Sample> start) implements CustomPacketPayload {
    public record Sample(Vec3 eye, float yaw, float pitch) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Sample> STREAM_CODEC = StreamCodec.composite(
            Vec3.STREAM_CODEC, Sample::eye,
            ByteBufCodecs.FLOAT, Sample::yaw,
            ByteBufCodecs.FLOAT, Sample::pitch,
            Sample::new
        );
    }

    public static final Type<ClockRewindMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "clock_rewind"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClockRewindMessage> STREAM_CODEC = StreamCodec.composite(
        Sample.STREAM_CODEC.apply(ByteBufCodecs.list(128)), ClockRewindMessage::recent,
        Sample.STREAM_CODEC.apply(ByteBufCodecs.list(128)), ClockRewindMessage::start,
        ClockRewindMessage::new
    );

    @Override
    public Type<ClockRewindMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, List<Sample> recent, List<Sample> start) {
        if (player.connection != null && player.connection.hasChannel(TYPE)) {
            PacketDistributor.sendToPlayer(player, new ClockRewindMessage(recent, start));
        }
    }

    public static void handleData(ClockRewindMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.start(message));
    }

    private static final class Client {
        static void start(ClockRewindMessage message) {
            ClockRewindClient.start(message.recent(), message.start());
        }
    }
}
