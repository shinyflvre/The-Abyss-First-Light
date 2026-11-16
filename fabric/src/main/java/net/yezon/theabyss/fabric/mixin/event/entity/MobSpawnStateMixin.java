package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.world.entity.Mob;
import net.yezon.theabyss.fabric.event.FabricMobSpawnState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Mob.class)
public abstract class MobSpawnStateMixin implements FabricMobSpawnState {
    @Unique
    private boolean theabyss$spawnCancelled;

    @Override
    public boolean theabyss$isSpawnCancelled() {
        return this.theabyss$spawnCancelled;
    }

    @Override
    public void theabyss$setSpawnCancelled(boolean cancelled) {
        this.theabyss$spawnCancelled = cancelled;
    }
}
