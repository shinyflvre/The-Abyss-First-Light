package net.yezon.theabyss.astral;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;

public record AstralData(boolean active) {
    public static final AstralData NONE = new AstralData(false);
    public static final MapCodec<AstralData> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(Codec.BOOL.fieldOf("active").forGetter(AstralData::active)).apply(i, AstralData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AstralData> STREAM_CODEC = ByteBufCodecs.BOOL
        .map(AstralData::new, AstralData::active)
        .cast();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<AstralData>> TYPE = ATTACHMENTS.register(
        "astral", () -> AttachmentType.builder(() -> NONE).serialize(CODEC, AstralData::active).sync(STREAM_CODEC).build()
    );

    public static boolean isAstral(Entity entity) {
        return entity instanceof Player player && player.getData(TYPE).active();
    }
}
