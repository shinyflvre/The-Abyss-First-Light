package net.yezon.theabyss.possession;

import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;

public record PossessionData(int mobId) {
    public static final PossessionData NONE = new PossessionData(-1);
    public static final StreamCodec<RegistryFriendlyByteBuf, PossessionData> STREAM_CODEC = ByteBufCodecs.VAR_INT
        .map(PossessionData::new, PossessionData::mobId)
        .cast();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<PossessionData>> TYPE = ATTACHMENTS.register(
        "possession", () -> AttachmentType.builder(() -> NONE).sync(STREAM_CODEC).build()
    );

    public boolean active() {
        return this.mobId >= 0;
    }

    public static PossessionData of(Player player) {
        return player.getData(TYPE);
    }
}
