package net.yezon.theabyss.fabric.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.Tag;

public final class FabricPersistentData {
    public static final String KEY = "NeoForgeData";
    public static final String PLAYER_PERSISTED = "PlayerPersisted";

    private FabricPersistentData() {
    }

    public static CompoundTag get(Entity entity) {
        return ((FabricPersistentDataHolder)entity).theabyss$persistentData();
    }

    public static void save(Entity entity, CompoundTag tag) {
        CompoundTag data = ((FabricPersistentDataHolder)entity).theabyss$persistentDataOrNull();
        if (data != null) {
            tag.put(KEY, data.copy());
        }
    }

    public static void load(Entity entity, CompoundTag tag) {
        if (tag.contains(KEY, Tag.TAG_COMPOUND)) {
            ((FabricPersistentDataHolder)entity).theabyss$setPersistentData(tag.getCompound(KEY));
        }
    }

    public static void copyPlayerPersisted(ServerPlayer from, ServerPlayer to) {
        CompoundTag old = get(from);
        if (old.contains(PLAYER_PERSISTED)) {
            get(to).put(PLAYER_PERSISTED, old.get(PLAYER_PERSISTED));
        }
    }
}
