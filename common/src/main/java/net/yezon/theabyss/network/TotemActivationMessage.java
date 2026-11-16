package net.yezon.theabyss.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.services.ServerHooks;

public record TotemActivationMessage(ItemStack stack) implements CustomPacketPayload {
    public static final Type<TotemActivationMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "totem_activation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TotemActivationMessage> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC, TotemActivationMessage::stack,
        TotemActivationMessage::new
    );

    @Override
    public Type<TotemActivationMessage> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, ItemStack stack) {
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, TYPE)) {
            PacketDistributor.sendToPlayer(player, new TotemActivationMessage(stack));
        }
    }

    public static void handleData(TotemActivationMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.displayItemActivation(message.stack()));
    }

    private static final class Client {
        static void displayItemActivation(ItemStack stack) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayItemActivation(stack);
            }
        }
    }
}
