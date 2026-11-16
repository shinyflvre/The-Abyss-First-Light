package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class AttackEntityEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.player.Player entity;
    private final net.minecraft.world.entity.Entity target;

    public AttackEntityEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.Entity target) {
        this.entity = entity;
        this.target = target;
    }

    public net.minecraft.world.entity.player.Player getEntity() {
        return this.entity;
    }

    public net.minecraft.world.entity.Entity getTarget() {
        return this.target;
    }
}
