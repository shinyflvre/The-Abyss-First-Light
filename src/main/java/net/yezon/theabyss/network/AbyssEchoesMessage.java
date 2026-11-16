package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.ability.AbyssEchoes;

public record AbyssEchoesMessage() implements CustomPacketPayload {
    public static final Type<AbyssEchoesMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "key_abyss_echoes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbyssEchoesMessage> STREAM_CODEC = StreamCodec.unit(new AbyssEchoesMessage());

    @Override
    public Type<AbyssEchoesMessage> type() {
        return TYPE;
    }

    public static void handleData(AbyssEchoesMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AbyssEchoes.activate(player);
            }
        });
    }
}
