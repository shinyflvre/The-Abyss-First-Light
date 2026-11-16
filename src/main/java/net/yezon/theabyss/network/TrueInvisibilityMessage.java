package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record TrueInvisibilityMessage(int entityId, int ticks, float radius) implements CustomPacketPayload {
    public static final Type<TrueInvisibilityMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "true_invisibility"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrueInvisibilityMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, TrueInvisibilityMessage::entityId,
        ByteBufCodecs.VAR_INT, TrueInvisibilityMessage::ticks,
        ByteBufCodecs.FLOAT, TrueInvisibilityMessage::radius,
        TrueInvisibilityMessage::new
    );

    @Override
    public Type<TrueInvisibilityMessage> type() {
        return TYPE;
    }

    public static void handleData(TrueInvisibilityMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(TrueInvisibilityMessage m) {
            net.yezon.theabyss.client.fx.TrueInvisibilityClient.set(m.entityId(), m.ticks(), m.radius());
        }
    }
}
