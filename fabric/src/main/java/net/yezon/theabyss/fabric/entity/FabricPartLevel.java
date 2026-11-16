package net.yezon.theabyss.fabric.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;

public interface FabricPartLevel {
    Int2ObjectMap<Entity> theabyss$parts();
}
