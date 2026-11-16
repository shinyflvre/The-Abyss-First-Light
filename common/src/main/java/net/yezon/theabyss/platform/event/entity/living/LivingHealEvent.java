package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingHealEvent extends Event implements ICancellableEvent {
    private float amount;
    private final net.minecraft.world.entity.LivingEntity entity;

    public LivingHealEvent(float amount, net.minecraft.world.entity.LivingEntity entity) {
        this.amount = amount;
        this.entity = entity;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setAmount(float value) {
        this.amount = value;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }
}
