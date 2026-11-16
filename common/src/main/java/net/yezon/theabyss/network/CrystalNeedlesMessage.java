package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ApplyRingOfCrystalsEvents;

public record CrystalNeedlesMessage() implements CustomPacketPayload {
    public static final Type<CrystalNeedlesMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "crystal_needles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalNeedlesMessage> STREAM_CODEC = StreamCodec.unit(new CrystalNeedlesMessage());

    @Override
    public Type<CrystalNeedlesMessage> type() {
        return TYPE;
    }

    public static void handleData(CrystalNeedlesMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                net.yezon.theabyss.ring.RingMagic.secondary(player);
            }
        });
    }
}
