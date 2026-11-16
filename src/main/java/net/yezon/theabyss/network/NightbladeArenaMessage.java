package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record NightbladeArenaMessage(double x, double y, double z, float radius, int durationTicks, boolean red) implements CustomPacketPayload {
    public static final Type<NightbladeArenaMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_arena"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeArenaMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, NightbladeArenaMessage::x,
        ByteBufCodecs.DOUBLE, NightbladeArenaMessage::y,
        ByteBufCodecs.DOUBLE, NightbladeArenaMessage::z,
        ByteBufCodecs.FLOAT, NightbladeArenaMessage::radius,
        ByteBufCodecs.VAR_INT, NightbladeArenaMessage::durationTicks,
        ByteBufCodecs.BOOL, NightbladeArenaMessage::red,
        NightbladeArenaMessage::new
    );

    @Override
    public Type<NightbladeArenaMessage> type() {
        return TYPE;
    }

    public static void around(ServerLevel level, Vec3 centre, float radius, int durationTicks, boolean red) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(centre) <= 96.0 && player.connection != null && player.connection.hasChannel(TYPE)) {
                PacketDistributor.sendToPlayer(player, new NightbladeArenaMessage(centre.x, centre.y, centre.z, radius, durationTicks, red));
            }
        }
    }

    public static void handleData(NightbladeArenaMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(NightbladeArenaMessage m) {
            net.yezon.theabyss.client.fx.NightbladeArenaClient.set(m.x(), m.y(), m.z(), m.radius(), m.durationTicks(), m.red());
        }
    }
}
