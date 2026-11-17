package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record RingFxMessage(int kind, double x, double y, double z, float radius, int ticks, int color, float param) implements CustomPacketPayload {
    public static final Type<RingFxMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "ring_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RingFxMessage> STREAM_CODEC = StreamCodec.of(
        (buf, m) -> {
            ByteBufCodecs.VAR_INT.encode(buf, m.kind());
            ByteBufCodecs.DOUBLE.encode(buf, m.x());
            ByteBufCodecs.DOUBLE.encode(buf, m.y());
            ByteBufCodecs.DOUBLE.encode(buf, m.z());
            ByteBufCodecs.FLOAT.encode(buf, m.radius());
            ByteBufCodecs.VAR_INT.encode(buf, m.ticks());
            ByteBufCodecs.INT.encode(buf, m.color());
            ByteBufCodecs.FLOAT.encode(buf, m.param());
        },
        buf -> new RingFxMessage(
            ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.DOUBLE.decode(buf), ByteBufCodecs.DOUBLE.decode(buf), ByteBufCodecs.DOUBLE.decode(buf),
            ByteBufCodecs.FLOAT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.INT.decode(buf), ByteBufCodecs.FLOAT.decode(buf)
        )
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
