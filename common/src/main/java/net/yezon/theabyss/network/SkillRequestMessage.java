package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.skill.Skill;
import net.yezon.theabyss.skill.Skills;

public record SkillRequestMessage(int action, String skill) implements CustomPacketPayload {
    public static final int LEARN = 0;
    public static final int FORGET = 1;
    public static final Type<SkillRequestMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "skill_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillRequestMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SkillRequestMessage::action,
        ByteBufCodecs.STRING_UTF8, SkillRequestMessage::skill,
        SkillRequestMessage::new
    );

    @Override
    public Type<SkillRequestMessage> type() {
        return TYPE;
    }

    public static void handleData(SkillRequestMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (message.action() == FORGET) {
                Skills.forget(player);
                return;
            }
            Skill skill = Skill.byId(message.skill());
            if (skill != null) {
                Skills.learn(player, skill);
            }
        });
    }
}
