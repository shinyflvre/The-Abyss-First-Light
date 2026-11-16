package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record ShadowVeilMessage(boolean active, int energy) implements CustomPacketPayload {
    public static final Type<ShadowVeilMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "shadow_veil"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShadowVeilMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ShadowVeilMessage::active,
        ByteBufCodecs.VAR_INT, ShadowVeilMessage::energy,
        ShadowVeilMessage::new
    );

    @Override
    public Type<ShadowVeilMessage> type() {
        return TYPE;
    }

    public static void handleData(ShadowVeilMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(ShadowVeilMessage m) {
            net.yezon.theabyss.client.fx.ShadowVeilClient.set(m.active(), m.energy());
        }
    }
}
