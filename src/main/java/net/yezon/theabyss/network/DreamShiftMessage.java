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

public record DreamShiftMessage() implements CustomPacketPayload {
    public static final Type<DreamShiftMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "dream_shift"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DreamShiftMessage> STREAM_CODEC = StreamCodec.unit(new DreamShiftMessage());

    @Override
    public Type<DreamShiftMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(TYPE)) {
            PacketDistributor.sendToPlayer(player, new DreamShiftMessage());
        }
    }

    public static void handleData(DreamShiftMessage message, IPayloadContext context) {
        context.enqueueWork(Client::start);
    }

    private static final class Client {
        static void start() {
            ScreenFx.startDreamShift();
        }
    }
}
