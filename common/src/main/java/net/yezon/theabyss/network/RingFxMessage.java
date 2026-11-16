package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record RingFxMessage(int kind, double x, double y, double z, float radius, int ticks, int color, float param) implements CustomPacketPayload {
    public static final Type<RingFxMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "ring_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RingFxMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, RingFxMessage::kind,
        ByteBufCodecs.DOUBLE, RingFxMessage::x,
        ByteBufCodecs.DOUBLE, RingFxMessage::y,
        ByteBufCodecs.DOUBLE, RingFxMessage::z,
        ByteBufCodecs.FLOAT, RingFxMessage::radius,
        ByteBufCodecs.VAR_INT, RingFxMessage::ticks,
        ByteBufCodecs.INT, RingFxMessage::color,
        ByteBufCodecs.FLOAT, RingFxMessage::param,
        RingFxMessage::new
    );

    @Override
    public Type<RingFxMessage> type() {
        return TYPE;
    }

    public static void handleData(RingFxMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.add(message));
    }

    private static final class Client {
        static void add(RingFxMessage m) {
            net.yezon.theabyss.client.fx.RingFxClient.add(m.kind(), m.x(), m.y(), m.z(), m.radius(), m.ticks(), m.color(), m.param());
        }
    }
}
