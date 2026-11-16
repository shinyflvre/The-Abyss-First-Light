package net.yezon.theabyss.network;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.platform.services.ServerHooks;

public record GravityMessage(int gravity, Vec3 feet) implements CustomPacketPayload {
    public static final Type<GravityMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "gravity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GravityMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GravityMessage::gravity,
        Vec3.STREAM_CODEC, GravityMessage::feet,
        GravityMessage::new
    );

    @Override
    public Type<GravityMessage> type() {
        return TYPE;
    }

    public static void handleServer(GravityMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Direction to = Direction.from3DDataValue(message.gravity());
            boolean valid = player.isAlive() && player.position().distanceToSqr(message.feet()) < 16.0
                && (to == Direction.DOWN || Gravity.fieldNear(player.level(), message.feet(), to));
            if (valid) {
                Gravity.acceptPlayer(player, to, message.feet());
            } else if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
                PacketDistributor.sendToPlayer(player, new GravityMessage(Gravity.of(player).get3DDataValue(), player.position()));
            }
        });
    }

    public static void handleClient(GravityMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(GravityMessage message) {
            net.yezon.theabyss.client.gravity.GravityClient.corrected(Direction.from3DDataValue(message.gravity()), message.feet());
        }
    }
}
