package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterItemPropertiesEvent extends Event {
    public abstract void register(Item item, ResourceLocation property, ClampedItemPropertyFunction function);

    public abstract void registerGeneric(ResourceLocation property, ClampedItemPropertyFunction function);
}
