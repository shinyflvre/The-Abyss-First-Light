package net.yezon.theabyss.platform.event.entity;

import net.yezon.theabyss.platform.event.Event;

public abstract class EntityAttributeCreationEvent extends Event {
    public abstract void put(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> a0, net.minecraft.world.entity.ai.attributes.AttributeSupplier a1);
}
