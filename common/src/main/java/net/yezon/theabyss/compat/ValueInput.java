package net.yezon.theabyss.compat;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.Nullable;

public final class ValueInput {
    private final CompoundTag tag;
    private final HolderLookup.@Nullable Provider registries;

    private ValueInput(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static ValueInput of(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        return new ValueInput(tag, registries);
    }

    public CompoundTag tag() {
        return this.tag;
    }

    public int getIntOr(String key, int fallback) {
        return NbtCompat.getIntOr(this.tag, key, fallback);
    }

    public long getLongOr(String key, long fallback) {
        return NbtCompat.getLongOr(this.tag, key, fallback);
    }

    public float getFloatOr(String key, float fallback) {
        return NbtCompat.getFloatOr(this.tag, key, fallback);
    }

    public double getDoubleOr(String key, double fallback) {
        return NbtCompat.getDoubleOr(this.tag, key, fallback);
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return NbtCompat.getBooleanOr(this.tag, key, fallback);
    }

    public String getStringOr(String key, String fallback) {
        return NbtCompat.getStringOr(this.tag, key, fallback);
    }

    public Optional<String> getString(String key) {
        return this.tag.contains(key, Tag.TAG_STRING) ? Optional.of(this.tag.getString(key)) : Optional.empty();
    }

    public Optional<ValueInput> child(String key) {
        return this.tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(new ValueInput(this.tag.getCompound(key), this.registries)) : Optional.empty();
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag value = this.tag.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return codec.parse(this.registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, this.registries), value).result();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }
}
