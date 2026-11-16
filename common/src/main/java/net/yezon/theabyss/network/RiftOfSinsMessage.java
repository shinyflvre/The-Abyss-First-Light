package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.rift.RiftOfSins;

public record RiftOfSinsMessage() implements CustomPacketPayload {
    public static final Type<RiftOfSinsMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "rift_of_sins"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RiftOfSinsMessage> STREAM_CODEC = StreamCodec.unit(new RiftOfSinsMessage());

    @Override
    public Type<RiftOfSinsMessage> type() {
        return TYPE;
    }

    public static void handleData(RiftOfSinsMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                RiftOfSins.toggle(player);
            }
        });
    }
}
