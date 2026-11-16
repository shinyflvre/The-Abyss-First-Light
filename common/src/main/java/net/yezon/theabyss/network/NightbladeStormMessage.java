package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record NightbladeStormMessage(int bossId, double x, double y, double z, int delayTicks, boolean red, boolean quiet) implements CustomPacketPayload {
    public static final Type<NightbladeStormMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_storm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeStormMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, NightbladeStormMessage::bossId,
        ByteBufCodecs.DOUBLE, NightbladeStormMessage::x,
        ByteBufCodecs.DOUBLE, NightbladeStormMessage::y,
        ByteBufCodecs.DOUBLE, NightbladeStormMessage::z,
        ByteBufCodecs.VAR_INT, NightbladeStormMessage::delayTicks,
        ByteBufCodecs.BOOL, NightbladeStormMessage::red,
        ByteBufCodecs.BOOL, NightbladeStormMessage::quiet,
        NightbladeStormMessage::new
    );

    @Override
    public Type<NightbladeStormMessage> type() {
        return TYPE;
    }

    public static void around(ServerLevel level, int bossId, Vec3 at, int delayTicks, boolean red) {
        around(level, bossId, at, delayTicks, red, false);
    }

    public static void around(ServerLevel level, int bossId, Vec3 at, int delayTicks, boolean red, boolean quiet) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(at) <= 64.0 && player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
                PacketDistributor.sendToPlayer(player, new NightbladeStormMessage(bossId, at.x, at.y, at.z, delayTicks, red, quiet));
            }
        }
    }

    public static void handleData(NightbladeStormMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.add(message));
    }

    private static final class Client {
        static void add(NightbladeStormMessage message) {
            net.yezon.theabyss.client.fx.NightbladeStormClient.addRing(message.bossId(), message.x(), message.y(), message.z(), message.delayTicks(), message.red(), message.quiet());
        }
    }
}
