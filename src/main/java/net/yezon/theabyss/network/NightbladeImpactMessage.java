package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record NightbladeImpactMessage(boolean parry) implements CustomPacketPayload {
    public static final Type<NightbladeImpactMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_impact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeImpactMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, NightbladeImpactMessage::parry,
        NightbladeImpactMessage::new
    );

    @Override
    public Type<NightbladeImpactMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, boolean parry) {
        if (player.connection != null && player.connection.hasChannel(TYPE)) {
            PacketDistributor.sendToPlayer(player, new NightbladeImpactMessage(parry));
        }
    }

    public static void handleData(NightbladeImpactMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.start(message.parry()));
    }

    private static final class Client {
        static void start(boolean parry) {
            net.yezon.theabyss.client.fx.ImpactFrame.start(parry);
        }
    }
}
