package net.yezon.theabyss.lore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;

public record ChronicleData(int pages, boolean bossDefeated, boolean diedInAbyss) {
    public static final int COUNT = 20;
    public static final ChronicleData NONE = new ChronicleData(0, false, false);
    public static final MapCodec<ChronicleData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("pages", 0).forGetter(ChronicleData::pages),
        Codec.BOOL.optionalFieldOf("boss_defeated", false).forGetter(ChronicleData::bossDefeated),
        Codec.BOOL.optionalFieldOf("died_in_abyss", false).forGetter(ChronicleData::diedInAbyss)
    ).apply(i, ChronicleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChronicleData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ChronicleData::pages,
        ByteBufCodecs.BOOL, ChronicleData::bossDefeated,
        ByteBufCodecs.BOOL, ChronicleData::diedInAbyss,
        ChronicleData::new
    );

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<ChronicleData>> TYPE = ATTACHMENTS.register(
        "chronicles", () -> AttachmentType.builder(() -> NONE)
            .serialize(CODEC, d -> d.pages != 0 || d.bossDefeated || d.diedInAbyss)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, STREAM_CODEC)
            .build()
    );

    public boolean has(int page) {
        return page >= 1 && page <= COUNT && (this.pages & (1 << (page - 1))) != 0;
    }

    public int count() {
        return Integer.bitCount(this.pages & ((1 << COUNT) - 1));
    }

    public ChronicleData with(int page) {
        return new ChronicleData(this.pages | 1 << (page - 1), this.bossDefeated, this.diedInAbyss);
    }

    public ChronicleData boss(boolean value) {
        return new ChronicleData(this.pages, value, this.diedInAbyss);
    }

    public ChronicleData died(boolean value) {
        return new ChronicleData(this.pages, this.bossDefeated, value);
    }

    public static ChronicleData of(Player player) {
        return net.yezon.theabyss.platform.attachment.Attachments.getData(player, TYPE);
    }
}
