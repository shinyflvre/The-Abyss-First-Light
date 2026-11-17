package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record ChronicleMessage(int page) implements CustomPacketPayload {
    public static final Type<ChronicleMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "chronicle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChronicleMessage> STREAM_CODEC = ByteBufCodecs.VAR_INT
        .map(ChronicleMessage::new, ChronicleMessage::page)
        .cast();

    @Override
    public Type<ChronicleMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int page) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new ChronicleMessage(page));
        }
    }

    public static void handleData(ChronicleMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.noted(message.page()));
    }

    private static final class Client {
        static void noted(int page) {
            net.yezon.theabyss.client.lore.ChronicleVoice.noted(page);
        }
    }
}
