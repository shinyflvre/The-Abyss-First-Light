package net.yezon.theabyss.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record TelekinesisMessage(int kind, int casterId, List<Integer> targets) implements CustomPacketPayload {
    public static final int START = 0;
    public static final int IMPACT = 1;
    public static final int PULL = 2;
    public static final int PULL_PARRY = 3;
    public static final Type<TelekinesisMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "telekinesis"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TelekinesisMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, TelekinesisMessage::kind,
        ByteBufCodecs.VAR_INT, TelekinesisMessage::casterId,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), TelekinesisMessage::targets,
        TelekinesisMessage::new
    );

    @Override
    public Type<TelekinesisMessage> type() {
        return TYPE;
    }

    public static void around(ServerLevel level, Vec3 at, TelekinesisMessage message) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(at) <= 96.0 && player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
                PacketDistributor.sendToPlayer(player, message);
            }
        }
    }

    public static void handleData(TelekinesisMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.handle(message));
    }

    private static final class Client {
        static void handle(TelekinesisMessage message) {
            if (message.kind() == START) {
                net.yezon.theabyss.client.fx.TelekinesisClient.start(message.casterId(), message.targets());
            } else if (message.kind() == PULL || message.kind() == PULL_PARRY) {
                net.yezon.theabyss.client.fx.TelekinesisClient.pull(message.casterId(), message.targets(), message.kind() == PULL_PARRY);
            } else if (!message.targets().isEmpty()) {
                net.yezon.theabyss.client.fx.TelekinesisClient.impact(message.casterId(), message.targets().get(0));
            }
        }
    }
}
