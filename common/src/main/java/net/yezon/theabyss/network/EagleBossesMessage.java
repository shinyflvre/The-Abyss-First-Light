package net.yezon.theabyss.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record EagleBossesMessage(List<Marker> markers, int ticks) implements CustomPacketPayload {
    public record Marker(String type, double x, double y, double z) {
        static final StreamCodec<RegistryFriendlyByteBuf, Marker> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Marker::type,
            ByteBufCodecs.DOUBLE, Marker::x,
            ByteBufCodecs.DOUBLE, Marker::y,
            ByteBufCodecs.DOUBLE, Marker::z,
            Marker::new
        );
    }

    public static final Type<EagleBossesMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "eagle_bosses"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EagleBossesMessage> STREAM_CODEC = StreamCodec.composite(
        Marker.STREAM_CODEC.apply(ByteBufCodecs.list(64)), EagleBossesMessage::markers,
        ByteBufCodecs.VAR_INT, EagleBossesMessage::ticks,
        EagleBossesMessage::new
    );

    @Override
    public Type<EagleBossesMessage> type() {
        return TYPE;
    }

    public static void handleData(EagleBossesMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(EagleBossesMessage m) {
            net.yezon.theabyss.client.fx.EagleBossCompass.set(m.markers(), m.ticks());
        }
    }
}
