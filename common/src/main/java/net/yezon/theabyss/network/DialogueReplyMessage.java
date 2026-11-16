package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.rift.RiftStory;

public record DialogueReplyMessage(int id, int choice) implements CustomPacketPayload {
    public static final int FINISHED = -1;
    public static final int CANCEL = -2;
    public static final Type<DialogueReplyMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "dialogue_reply"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueReplyMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, DialogueReplyMessage::id,
        ByteBufCodecs.INT, DialogueReplyMessage::choice,
        DialogueReplyMessage::new
    );

    @Override
    public Type<DialogueReplyMessage> type() {
        return TYPE;
    }

    public static void handleData(DialogueReplyMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                RiftStory.onReply(player, message.id(), message.choice());
            }
        });
    }
}
