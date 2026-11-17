package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ApplyGhostAttackEvents;

public record GhostAttackMessage(int eventType, int pressedms) implements CustomPacketPayload {
    public static final Type<GhostAttackMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "key_ghost_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostAttackMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, GhostAttackMessage::eventType,
        ByteBufCodecs.INT, GhostAttackMessage::pressedms,
        GhostAttackMessage::new
    );

    @Override
    public Type<GhostAttackMessage> type() {
        return TYPE;
    }

    public static void handleData(GhostAttackMessage message, IPayloadContext context) {
        context.enqueueWork(() -> pressAction(context.player(), message.eventType(), message.pressedms()));
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        if (!world.isClientSide() && world.hasChunkAt(entity.blockPosition())) {
            if (type == 0) {
                ApplyGhostAttackEvents.execute(world, x, y, z, entity);
            }
        }
    }
}
