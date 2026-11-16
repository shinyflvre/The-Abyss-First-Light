package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;

public record NightbladeParryMessage() implements CustomPacketPayload {
    public static final Type<NightbladeParryMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_parry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeParryMessage> STREAM_CODEC = StreamCodec.unit(new NightbladeParryMessage());

    @Override
    public Type<NightbladeParryMessage> type() {
        return TYPE;
    }

    public static void handleData(NightbladeParryMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                NightbladeParry.attempt(player);
            }
        });
    }
}
