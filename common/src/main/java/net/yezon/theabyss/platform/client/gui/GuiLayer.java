package net.yezon.theabyss.platform.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface GuiLayer {
    void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker);
}
