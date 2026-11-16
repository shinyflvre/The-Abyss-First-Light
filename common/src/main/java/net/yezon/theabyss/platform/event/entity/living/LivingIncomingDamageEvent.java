package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingIncomingDamageEvent extends Event implements ICancellableEvent {
    private float amount;
    private final net.minecraft.world.entity.LivingEntity entity;
    private final net.minecraft.world.damagesource.DamageSource source;

    public LivingIncomingDamageEvent(float amount, net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        this.amount = amount;
        this.entity = entity;
        this.source = source;
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

    public net.minecraft.world.damagesource.DamageSource getSource() {
        return this.source;
    }
}
