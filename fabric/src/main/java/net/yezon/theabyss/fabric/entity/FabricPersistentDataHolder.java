package net.yezon.theabyss.fabric.entity;

import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;

public interface FabricPersistentDataHolder {
    @Nullable CompoundTag theabyss$persistentDataOrNull();

    CompoundTag theabyss$persistentData();

    void theabyss$setPersistentData(CompoundTag data);
}
