package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterGuiLayersEvent extends Event {
    public abstract void registerAbove(net.minecraft.resources.Identifier a0, net.minecraft.resources.Identifier a1, net.yezon.theabyss.platform.client.gui.GuiLayer a2);
    public abstract void registerBelow(net.minecraft.resources.Identifier a0, net.minecraft.resources.Identifier a1, net.yezon.theabyss.platform.client.gui.GuiLayer a2);
}
