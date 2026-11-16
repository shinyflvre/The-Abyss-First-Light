package net.yezon.theabyss.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;

public record SkillSyncMessage(List<String> learned, int spent, boolean open) implements CustomPacketPayload {
    public static final Type<SkillSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "skill_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSyncMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SkillSyncMessage::learned,
        ByteBufCodecs.VAR_INT, SkillSyncMessage::spent,
        ByteBufCodecs.BOOL, SkillSyncMessage::open,
        SkillSyncMessage::new
    );

    @Override
    public Type<SkillSyncMessage> type() {
        return TYPE;
    }

    public static void handleData(SkillSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> Client.apply(message));
    }

    private static final class Client {
        static void apply(SkillSyncMessage m) {
            net.yezon.theabyss.client.skill.SkillClient.sync(m.learned(), m.spent(), m.open());
        }
    }
}
