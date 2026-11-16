package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingFallEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.LivingEntity entity;

    public LivingFallEvent(net.minecraft.world.entity.LivingEntity entity) {
        this.entity = entity;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }
}
