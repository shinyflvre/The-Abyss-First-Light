package net.yezon.theabyss.platform.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.platform.event.Event;
import org.jspecify.annotations.Nullable;

public abstract class RegisterSpawnPlacementsEvent extends Event {
    public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        this.register(entityType, null, null, predicate, Operation.OR);
    }

    public <T extends Entity> void register(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
        this.register(entityType, null, null, predicate, operation);
    }

    public abstract <T extends Entity> void register(EntityType<T> entityType, @Nullable SpawnPlacementType placementType, Heightmap.@Nullable Types heightmap,
        SpawnPlacements.SpawnPredicate<T> predicate, Operation operation);

    public enum Operation {
        AND,
        OR,
        REPLACE
    }
}
