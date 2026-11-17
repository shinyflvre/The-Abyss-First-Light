package net.yezon.theabyss.fabric.mixin.entity;

import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsAccessor {
    @Accessor("DATA_BY_TYPE")
    static Map<EntityType<?>, Object> theabyss$data() {
        throw new AssertionError();
    }

    @Invoker("register")
    static <T extends Mob> void theabyss$register(EntityType<T> type, SpawnPlacementType placementType, Heightmap.Types heightmap,
        SpawnPlacements.SpawnPredicate<T> predicate) {
        throw new AssertionError();
    }
}
