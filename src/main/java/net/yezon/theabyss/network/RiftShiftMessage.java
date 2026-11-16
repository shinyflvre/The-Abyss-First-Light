package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.ScreenFx;

public record RiftShiftMessage() implements CustomPacketPayload {
    public static final Type<RiftShiftMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "rift_shift"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RiftShiftMessage> STREAM_CODEC = StreamCodec.unit(new RiftShiftMessage());

    @Override
    public Type<RiftShiftMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(TYPE)) {
            PacketDistributor.sendToPlayer(player, new RiftShiftMessage());
        }
    }

    public static void handleData(RiftShiftMessage message, IPayloadContext context) {
        context.enqueueWork(Client::start);
    }

    private static final class Client {
        static void start() {
            ScreenFx.startRiftShift();
        }
    }
}
