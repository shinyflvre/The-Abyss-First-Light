package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record OvergrowthMessage(int entityId, float stage, int flags, int collapse) implements CustomPacketPayload {
    public static final int BEAT = 1;
    public static final int STAGGER = 2;
    public static final Type<OvergrowthMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "overgrowth"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OvergrowthMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, OvergrowthMessage::entityId,
        ByteBufCodecs.FLOAT, OvergrowthMessage::stage,
        ByteBufCodecs.VAR_INT, OvergrowthMessage::flags,
        ByteBufCodecs.VAR_INT, OvergrowthMessage::collapse,
        OvergrowthMessage::new
    );

    @Override
    public Type<OvergrowthMessage> type() {
        return TYPE;
    }

    public static void handleData(OvergrowthMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(OvergrowthMessage m) {
            net.yezon.theabyss.client.fx.OvergrowthClient.update(m.entityId(), m.stage(), m.flags(), m.collapse());
        }
    }
}
