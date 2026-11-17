package net.yezon.theabyss.platform.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface GuiLayer {
    void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
}
