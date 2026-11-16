package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record TeleportDashMessage(double x, double y, double z, float yaw, float pitch) implements CustomPacketPayload {
    public static final Type<TeleportDashMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "teleport_dash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportDashMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, TeleportDashMessage::x,
        ByteBufCodecs.DOUBLE, TeleportDashMessage::y,
        ByteBufCodecs.DOUBLE, TeleportDashMessage::z,
        ByteBufCodecs.FLOAT, TeleportDashMessage::yaw,
        ByteBufCodecs.FLOAT, TeleportDashMessage::pitch,
        TeleportDashMessage::new
    );

    @Override
    public Type<TeleportDashMessage> type() {
        return TYPE;
    }

    public static void handleData(TeleportDashMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(TeleportDashMessage m) {
            net.yezon.theabyss.client.fx.TeleportDashClient.start(new net.minecraft.world.phys.Vec3(m.x(), m.y(), m.z()), m.yaw(), m.pitch());
        }
    }
}
