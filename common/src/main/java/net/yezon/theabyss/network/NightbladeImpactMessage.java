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

public record NightbladeImpactMessage(boolean parry) implements CustomPacketPayload {
    public static final Type<NightbladeImpactMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_impact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeImpactMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, NightbladeImpactMessage::parry,
        NightbladeImpactMessage::new
    );

    @Override
    public Type<NightbladeImpactMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, boolean parry) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
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
