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
import net.yezon.theabyss.events.Friendly2Events;

public record FriendleyFireBMessage(int eventType, int pressedms) implements CustomPacketPayload {
    public static final Type<FriendleyFireBMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "key_friendley_fire_b"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FriendleyFireBMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, FriendleyFireBMessage::eventType,
        ByteBufCodecs.INT, FriendleyFireBMessage::pressedms,
        FriendleyFireBMessage::new
    );

    @Override
    public Type<FriendleyFireBMessage> type() {
        return TYPE;
    }

    public static void handleData(FriendleyFireBMessage message, IPayloadContext context) {
        context.enqueueWork(() -> pressAction(context.player(), message.eventType(), message.pressedms()));
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        if (world.hasChunkAt(entity.blockPosition())) {
            if (type == 0) {
                Friendly2Events.execute(world, x, y, z, entity);
            }
        }
    }
}
