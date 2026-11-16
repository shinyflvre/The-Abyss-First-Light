package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record AbilDebugMessage(int eventType, int pressedms) implements CustomPacketPayload {
    public static final Type<AbilDebugMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "key_abil_debug"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilDebugMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, AbilDebugMessage::eventType,
        ByteBufCodecs.INT, AbilDebugMessage::pressedms,
        AbilDebugMessage::new
    );

    @Override
    public Type<AbilDebugMessage> type() {
        return TYPE;
    }

    public static void handleData(AbilDebugMessage message, IPayloadContext context) {
    }
}
