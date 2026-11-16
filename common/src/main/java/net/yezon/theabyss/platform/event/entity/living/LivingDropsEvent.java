package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingDropsEvent extends Event implements ICancellableEvent {
    private final java.util.Collection<net.minecraft.world.entity.item.ItemEntity> drops;
    private final net.minecraft.world.entity.LivingEntity entity;

    public LivingDropsEvent(java.util.Collection<net.minecraft.world.entity.item.ItemEntity> drops, net.minecraft.world.entity.LivingEntity entity) {
        this.drops = drops;
        this.entity = entity;
    }

    public java.util.Collection<net.minecraft.world.entity.item.ItemEntity> getDrops() {
        return this.drops;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }
}
