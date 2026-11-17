package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.RingMagic;

public record GauntletMessage(int action, int value) implements CustomPacketPayload {
    public static final int SELECT = 1;
    public static final int MAIN = 2;
    public static final int SECOND = 3;
    public static final Type<GauntletMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "gauntlet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GauntletMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GauntletMessage::action,
        ByteBufCodecs.VAR_INT, GauntletMessage::value,
        GauntletMessage::new
    );

    @Override
    public Type<GauntletMessage> type() {
        return TYPE;
    }

    public static void handleData(GauntletMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) {
                return;
            }
            switch (message.action()) {
                case SELECT -> {
                    ItemStack gauntlet = Gauntlet.held(player);
                    int slot = message.value();
                    if (!gauntlet.isEmpty() && slot >= 0 && slot < Gauntlet.SLOTS && !Gauntlet.rings(gauntlet).get(slot).isEmpty()) {
                        Gauntlet.select(gauntlet, slot);
                    }
                }
                case MAIN -> RingMagic.main(player);
                case SECOND -> RingMagic.secondary(player);
                default -> {
                }
            }
        });
    }
}
