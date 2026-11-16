package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingChangeTargetEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.LivingEntity entity;
    private final net.minecraft.world.entity.LivingEntity newAboutToBeSetTarget;

    public LivingChangeTargetEvent(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.LivingEntity newAboutToBeSetTarget) {
        this.entity = entity;
        this.newAboutToBeSetTarget = newAboutToBeSetTarget;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }

    public net.minecraft.world.entity.LivingEntity getNewAboutToBeSetTarget() {
        return this.newAboutToBeSetTarget;
    }
}
