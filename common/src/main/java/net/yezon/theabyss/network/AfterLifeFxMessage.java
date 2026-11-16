package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.AfterLifeClient;
import net.yezon.theabyss.platform.services.ServerHooks;

public record AfterLifeFxMessage(int kind, Vec3 body, int ticks) implements CustomPacketPayload {
    public static final int START = 0;
    public static final int FOUND = 1;
    public static final int END = 2;
    public static final Type<AfterLifeFxMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "after_life_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AfterLifeFxMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, AfterLifeFxMessage::kind,
        Vec3.STREAM_CODEC, AfterLifeFxMessage::body,
        ByteBufCodecs.VAR_INT, AfterLifeFxMessage::ticks,
        AfterLifeFxMessage::new
    );

    @Override
    public Type<AfterLifeFxMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int kind, Vec3 body, int ticks) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new AfterLifeFxMessage(kind, body, ticks));
        }
    }

    public static void handleData(AfterLifeFxMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.handle(message));
    }

    private static final class Client {
        static void handle(AfterLifeFxMessage message) {
            switch (message.kind()) {
                case START -> AfterLifeClient.start(message.body(), message.ticks());
                case FOUND -> AfterLifeClient.found();
                default -> AfterLifeClient.end();
            }
        }
    }
}
