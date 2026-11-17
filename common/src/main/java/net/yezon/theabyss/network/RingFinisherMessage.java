package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record RingFinisherMessage() implements CustomPacketPayload {
    public static final Type<RingFinisherMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "ring_finisher"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RingFinisherMessage> STREAM_CODEC = StreamCodec.unit(new RingFinisherMessage());

    @Override
    public Type<RingFinisherMessage> type() {
        return TYPE;
    }

    public static void handleData(RingFinisherMessage message, IPayloadContext context) {
        context.enqueueWork(Client::play);
    }

    private static final class Client {
        static void play() {
            net.yezon.theabyss.client.fx.RingFinisherClient.play();
        }
    }
}
