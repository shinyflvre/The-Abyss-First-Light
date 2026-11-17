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
import net.yezon.theabyss.client.fx.NightGraceClient;
import net.yezon.theabyss.platform.services.ServerHooks;

public record NightGraceMessage(int entityId, int ticks) implements CustomPacketPayload {
    public static final Type<NightGraceMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "night_grace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightGraceMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, NightGraceMessage::entityId,
        ByteBufCodecs.VAR_INT, NightGraceMessage::ticks,
        NightGraceMessage::new
    );

    @Override
    public Type<NightGraceMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int entityId, int ticks) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new NightGraceMessage(entityId, ticks));
        }
    }

    public static void handleData(NightGraceMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.start(message.entityId(), message.ticks()));
    }

    private static final class Client {
        static void start(int entityId, int ticks) {
            NightGraceClient.start(entityId, ticks);
        }
    }
}
