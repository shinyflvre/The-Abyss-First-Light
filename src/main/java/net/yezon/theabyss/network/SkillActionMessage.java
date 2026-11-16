package net.yezon.theabyss.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.skill.ShadowDash;
import net.yezon.theabyss.skill.WraithForm;

public record SkillActionMessage(int action, int value) implements CustomPacketPayload {
    public static final int DASH = 0;
    public static final int WRAITH = 1;
    public static final Type<SkillActionMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "skill_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillActionMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SkillActionMessage::action,
        ByteBufCodecs.VAR_INT, SkillActionMessage::value,
        SkillActionMessage::new
    );

    @Override
    public Type<SkillActionMessage> type() {
        return TYPE;
    }

    public static void handleData(SkillActionMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() || !player.isAlive()) {
                return;
            }
            if (message.action() == DASH) {
                ShadowDash.dash(player);
            } else if (message.action() == WRAITH) {
                WraithForm.toggle(player);
            }
        });
    }
}
