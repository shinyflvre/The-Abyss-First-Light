package net.yezon.theabyss.platform.event.entity;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class EntityJoinLevelEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.Entity entity;
    private final net.minecraft.world.level.Level level;
    private final boolean loadedFromDisk;

    public EntityJoinLevelEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.level.Level level, boolean loadedFromDisk) {
        this.entity = entity;
        this.level = level;
        this.loadedFromDisk = loadedFromDisk;
    }

    public net.minecraft.world.entity.Entity getEntity() {
        return this.entity;
    }

    public net.minecraft.world.level.Level getLevel() {
        return this.level;
    }

    public boolean loadedFromDisk() {
        return this.loadedFromDisk;
    }
}
