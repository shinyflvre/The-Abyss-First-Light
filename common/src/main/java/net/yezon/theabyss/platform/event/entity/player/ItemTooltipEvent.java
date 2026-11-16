package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;

public class ItemTooltipEvent extends Event {
    private final net.minecraft.world.entity.player.Player entity;
    private final net.minecraft.world.item.TooltipFlag flags;
    private final net.minecraft.world.item.ItemStack itemStack;
    private final java.util.List<net.minecraft.network.chat.Component> toolTip;

    public ItemTooltipEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.item.TooltipFlag flags, net.minecraft.world.item.ItemStack itemStack, java.util.List<net.minecraft.network.chat.Component> toolTip) {
        this.entity = entity;
        this.flags = flags;
        this.itemStack = itemStack;
        this.toolTip = toolTip;
    }

    public net.minecraft.world.entity.player.Player getEntity() {
        return this.entity;
    }

    public net.minecraft.world.item.TooltipFlag getFlags() {
        return this.flags;
    }

    public net.minecraft.world.item.ItemStack getItemStack() {
        return this.itemStack;
    }

    public java.util.List<net.minecraft.network.chat.Component> getToolTip() {
        return this.toolTip;
    }
}
