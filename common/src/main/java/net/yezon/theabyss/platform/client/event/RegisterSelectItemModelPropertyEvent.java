package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterSelectItemModelPropertyEvent extends Event {
    public abstract void register(net.minecraft.resources.Identifier a0, net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty.Type<?, ?> a1);
}
