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
import net.yezon.theabyss.events.Friendly1Events;

public record FriendleyFireAMessage(int eventType, int pressedms) implements CustomPacketPayload {
    public static final Type<FriendleyFireAMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "key_friendley_fire_a"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FriendleyFireAMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, FriendleyFireAMessage::eventType,
        ByteBufCodecs.INT, FriendleyFireAMessage::pressedms,
        FriendleyFireAMessage::new
    );

    @Override
    public Type<FriendleyFireAMessage> type() {
        return TYPE;
    }

    public static void handleData(FriendleyFireAMessage message, IPayloadContext context) {
        context.enqueueWork(() -> pressAction(context.player(), message.eventType(), message.pressedms()));
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        if (world.hasChunkAt(entity.blockPosition())) {
            if (type == 0) {
                Friendly1Events.execute(world, x, y, z, entity);
            }
        }
    }
}
