package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ApplyGhostFireAttackEvents;

public record GhostFireAttackMessage(int eventType, int pressedms) implements CustomPacketPayload {
    public static final Type<GhostFireAttackMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "key_ghost_fire_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostFireAttackMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, GhostFireAttackMessage::eventType,
        ByteBufCodecs.INT, GhostFireAttackMessage::pressedms,
        GhostFireAttackMessage::new
    );

    @Override
    public Type<GhostFireAttackMessage> type() {
        return TYPE;
    }

    public static void handleData(GhostFireAttackMessage message, IPayloadContext context) {
        context.enqueueWork(() -> pressAction(context.player(), message.eventType(), message.pressedms()));
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        if (!world.isClientSide() && world.hasChunkAt(entity.blockPosition())) {
            if (type == 0) {
                ApplyGhostFireAttackEvents.execute(world, x, y, z, entity);
            }
        }
    }
}
