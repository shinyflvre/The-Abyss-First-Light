package net.yezon.theabyss.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ResearchTableHandlerEvents;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public record ResearchButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<ResearchButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "research_buttons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchButtonMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ResearchButtonMessage::buttonID,
        ByteBufCodecs.INT, ResearchButtonMessage::x,
        ByteBufCodecs.INT, ResearchButtonMessage::y,
        ByteBufCodecs.INT, ResearchButtonMessage::z,
        ResearchButtonMessage::new
    );

    @Override
    public Type<ResearchButtonMessage> type() {
        return TYPE;
    }

    public static void handleData(ResearchButtonMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID(), message.x(), message.y(), message.z()));
    }

    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        if (!(entity.containerMenu instanceof ResearchMenu)) {
            return;
        }
        if (world.hasChunkAt(new BlockPos(x, y, z)) && buttonID == 0) {
            ResearchTableHandlerEvents.execute(entity);
        }
    }
}
