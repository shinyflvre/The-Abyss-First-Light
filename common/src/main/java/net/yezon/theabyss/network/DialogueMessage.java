package net.yezon.theabyss.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.dialogue.DialogueClient;
import net.yezon.theabyss.platform.services.ServerHooks;

public record DialogueMessage(int id, String speaker, List<String> lines, List<String> choices, boolean hold) implements CustomPacketPayload {
    public static final Type<DialogueMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, DialogueMessage::id,
        ByteBufCodecs.STRING_UTF8, DialogueMessage::speaker,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DialogueMessage::lines,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DialogueMessage::choices,
        ByteBufCodecs.BOOL, DialogueMessage::hold,
        DialogueMessage::new
    );

    @Override
    public Type<DialogueMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, DialogueMessage message) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, message);
        }
    }

    public static void handleData(DialogueMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.show(message));
    }

    private static final class Client {
        static void show(DialogueMessage message) {
            DialogueClient.show(message);
        }
    }
}
