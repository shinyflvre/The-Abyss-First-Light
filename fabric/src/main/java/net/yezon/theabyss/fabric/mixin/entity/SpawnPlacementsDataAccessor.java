package net.yezon.theabyss.fabric.mixin.entity;

import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.SpawnPlacements$Data")
public interface SpawnPlacementsDataAccessor {
    @Accessor("predicate")
    SpawnPlacements.SpawnPredicate<?> theabyss$predicate();
}
