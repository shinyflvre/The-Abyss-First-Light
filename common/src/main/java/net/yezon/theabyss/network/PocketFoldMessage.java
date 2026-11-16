package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record PocketFoldMessage() implements CustomPacketPayload {
    public static final Type<PocketFoldMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pocket_fold"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PocketFoldMessage> STREAM_CODEC = StreamCodec.unit(new PocketFoldMessage());

    @Override
    public Type<PocketFoldMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new PocketFoldMessage());
        }
    }

    public static void handleData(PocketFoldMessage message, IPayloadContext context) {
        context.enqueueWork(Client::start);
    }

    private static final class Client {
        static void start() {
            net.yezon.theabyss.client.fx.ScreenFx.startPocketFold();
        }
    }
}
