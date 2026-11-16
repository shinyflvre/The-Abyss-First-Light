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

public record NightbladeShakeMessage(float strength, float seconds) implements CustomPacketPayload {
    public static final Type<NightbladeShakeMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NightbladeShakeMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, NightbladeShakeMessage::strength, ByteBufCodecs.FLOAT, NightbladeShakeMessage::seconds, NightbladeShakeMessage::new);

    @Override
    public Type<NightbladeShakeMessage> type() {
        return TYPE;
    }

    public static void around(ServerLevel level, Vec3 centre, double radius, float strength, float seconds) {
        for (ServerPlayer player : level.players()) {
            double distance = player.position().distanceTo(centre);
            if (distance <= radius && player.connection != null && player.connection.hasChannel(TYPE)) {
                float falloff = (float)(1.0 - distance / radius * 0.7);
                PacketDistributor.sendToPlayer(player, new NightbladeShakeMessage(strength * falloff, seconds));
            }
        }
    }

    public static void handleData(NightbladeShakeMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.shake(message));
    }

    private static final class Client {
        static void shake(NightbladeShakeMessage message) {
            net.yezon.theabyss.client.fx.CameraShake.add(message.strength(), message.seconds());
        }
    }
}
