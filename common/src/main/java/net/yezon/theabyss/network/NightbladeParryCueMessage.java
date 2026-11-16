package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record NightbladeParryCueMessage(int sourceId, int impactIn, int window) implements CustomPacketPayload {
    public static final Type<NightbladeParryCueMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_parry_cue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeParryCueMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, NightbladeParryCueMessage::sourceId,
        ByteBufCodecs.VAR_INT, NightbladeParryCueMessage::impactIn,
        ByteBufCodecs.VAR_INT, NightbladeParryCueMessage::window,
        NightbladeParryCueMessage::new
    );

    @Override
    public Type<NightbladeParryCueMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int sourceId, int impactIn, int window) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new NightbladeParryCueMessage(sourceId, impactIn, window));
        }
    }

    public static void handleData(NightbladeParryCueMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.cue(message));
    }

    private static final class Client {
        static void cue(NightbladeParryCueMessage message) {
            net.yezon.theabyss.client.fx.ParryCue.set(message.sourceId(), message.impactIn(), message.window());
        }
    }
}
