package net.yezon.theabyss.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.possession.PossessionData;
import net.yezon.theabyss.possession.PossessionManager;

public record PossessionSprintMessage(boolean sprint) implements CustomPacketPayload {
    public static final Type<PossessionSprintMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "possession_sprint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PossessionSprintMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, PossessionSprintMessage::sprint,
        PossessionSprintMessage::new
    );

    @Override
    public Type<PossessionSprintMessage> type() {
        return TYPE;
    }

    public static void handleData(PossessionSprintMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PossessionManager.setSprinting(player, message.sprint());
            }
        });
    }

    @EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
    public static final class Client {
        private static boolean sent;

        private Client() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            boolean sprint = player != null && mc.getConnection() != null && PossessionData.of(player).active() && mc.options.keySprint.isDown();
            if (sprint != sent && player != null && mc.getConnection() != null) {
                sent = sprint;
                ClientPacketDistributor.sendToServer(new PossessionSprintMessage(sprint));
            } else if (player == null) {
                sent = false;
            }
        }
    }
}
