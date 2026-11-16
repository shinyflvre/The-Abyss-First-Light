package net.yezon.theabyss.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class PersistentData {
    private PersistentData() {
    }

    public static CompoundTag get(Entity entity) {
        return Platform.get().persistentData(entity);
    }
}
