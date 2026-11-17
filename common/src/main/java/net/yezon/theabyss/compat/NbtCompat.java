package net.yezon.theabyss.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class NbtCompat {
    private NbtCompat() {
    }

    public static int getIntOr(CompoundTag tag, String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    public static long getLongOr(CompoundTag tag, String key, long fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
    }

    public static float getFloatOr(CompoundTag tag, String key, float fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getFloat(key) : fallback;
    }

    public static double getDoubleOr(CompoundTag tag, String key, double fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : fallback;
    }

    public static byte getByteOr(CompoundTag tag, String key, byte fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getByte(key) : fallback;
    }

    public static short getShortOr(CompoundTag tag, String key, short fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getShort(key) : fallback;
    }

    public static boolean getBooleanOr(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    public static String getStringOr(CompoundTag tag, String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    public static CompoundTag getCompoundOrEmpty(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : new CompoundTag();
    }

    public static ListTag getListOrEmpty(CompoundTag tag, String key) {
        return tag.get(key) instanceof ListTag list ? list : new ListTag();
    }
}
