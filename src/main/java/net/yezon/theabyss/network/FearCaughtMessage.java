package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;

public record FearCaughtMessage() implements CustomPacketPayload {
    public static final Type<FearCaughtMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "fear_caught"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FearCaughtMessage> STREAM_CODEC = StreamCodec.unit(new FearCaughtMessage());
    private static final float DAMAGE = 4.0F;

    @Override
    public Type<FearCaughtMessage> type() {
        return TYPE;
    }

    public static void handleData(FearCaughtMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.hasEffect(TheabyssModMobEffects.FEAR)) {
                player.removeEffect(TheabyssModMobEffects.FEAR);
                player.hurtServer(player.level(), player.damageSources().magic(), DAMAGE);
            }
        });
    }
}
