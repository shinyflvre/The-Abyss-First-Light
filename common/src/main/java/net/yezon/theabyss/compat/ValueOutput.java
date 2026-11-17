package net.yezon.theabyss.compat;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.Nullable;

public final class ValueOutput {
    private final CompoundTag tag;
    private final HolderLookup.@Nullable Provider registries;

    private ValueOutput(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static ValueOutput of(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        return new ValueOutput(tag, registries);
    }

    public CompoundTag tag() {
        return this.tag;
    }

    public void putInt(String key, int value) {
        this.tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        this.tag.putLong(key, value);
    }

    public void putFloat(String key, float value) {
        this.tag.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        this.tag.putDouble(key, value);
    }

    public void putBoolean(String key, boolean value) {
        this.tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        this.tag.putString(key, value);
    }

    public ValueOutput child(String key) {
        CompoundTag child = new CompoundTag();
        this.tag.put(key, child);
        return new ValueOutput(child, this.registries);
    }

    public void discard(String key) {
        this.tag.remove(key);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(this.registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, this.registries), value)
            .result().ifPresent(encoded -> this.tag.put(key, encoded));
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }
}
