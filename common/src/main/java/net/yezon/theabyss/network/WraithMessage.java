package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record WraithMessage(int entityId, int ticks) implements CustomPacketPayload {
    public static final Type<WraithMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "wraith"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WraithMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, WraithMessage::entityId,
        ByteBufCodecs.VAR_INT, WraithMessage::ticks,
        WraithMessage::new
    );

    @Override
    public Type<WraithMessage> type() {
        return TYPE;
    }

    public static void handleData(WraithMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(WraithMessage m) {
            net.yezon.theabyss.client.skill.WraithClient.set(m.entityId(), m.ticks());
        }
    }
}
