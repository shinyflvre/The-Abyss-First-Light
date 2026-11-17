package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class FinalizeSpawnEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.Mob entity;
    private final net.minecraft.world.entity.MobSpawnType spawnType;
    private boolean spawnCancelled;

    public FinalizeSpawnEvent(net.minecraft.world.entity.Mob entity, net.minecraft.world.entity.MobSpawnType spawnType, boolean spawnCancelled) {
        this.entity = entity;
        this.spawnType = spawnType;
        this.spawnCancelled = spawnCancelled;
    }

    public net.minecraft.world.entity.Mob getEntity() {
        return this.entity;
    }

    public net.minecraft.world.entity.MobSpawnType getSpawnType() {
        return this.spawnType;
    }

    public boolean getSpawnCancelled() {
        return this.spawnCancelled;
    }

    public void setSpawnCancelled(boolean value) {
        this.spawnCancelled = value;
    }
}
