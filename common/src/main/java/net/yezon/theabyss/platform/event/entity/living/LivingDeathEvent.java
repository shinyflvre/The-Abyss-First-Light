package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class LivingDeathEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.LivingEntity entity;
    private final net.minecraft.world.damagesource.DamageSource source;

    public LivingDeathEvent(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        this.entity = entity;
        this.source = source;
    }

    public net.minecraft.world.entity.LivingEntity getEntity() {
        return this.entity;
    }

    public net.minecraft.world.damagesource.DamageSource getSource() {
        return this.source;
    }
}
