package net.yezon.theabyss.fabric.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class FabricPersistentData {
    public static final String KEY = "NeoForgeData";
    public static final String PLAYER_PERSISTED = "PlayerPersisted";

    private FabricPersistentData() {
    }

    public static CompoundTag get(Entity entity) {
        return ((FabricPersistentDataHolder)entity).theabyss$persistentData();
    }

    public static void save(Entity entity, ValueOutput output) {
        output.storeNullable(KEY, CompoundTag.CODEC, ((FabricPersistentDataHolder)entity).theabyss$persistentDataOrNull());
    }

    public static void load(Entity entity, ValueInput input) {
        input.read(KEY, CompoundTag.CODEC).ifPresent(((FabricPersistentDataHolder)entity)::theabyss$setPersistentData);
    }

    public static void copyPlayerPersisted(ServerPlayer from, ServerPlayer to) {
        CompoundTag old = get(from);
        if (old.contains(PLAYER_PERSISTED)) {
            get(to).put(PLAYER_PERSISTED, old.get(PLAYER_PERSISTED));
        }
    }
}
