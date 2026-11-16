package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.yezon.theabyss.events.IntroScreenHandlerEvents;

public final class IntroScreenOverlay {
    private IntroScreenOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, LocalPlayer entity) {
        if (IntroScreenHandlerEvents.execute(entity)) {
            int w = graphics.guiWidth();
            int h = graphics.guiHeight();
            graphics.fill(0, 0, w, h, TheAbyssScreenOverlay.LOADING_COLOUR);
            graphics.text(Minecraft.getInstance().font, Component.translatable("gui.theabyss.intro_screen.label_loading"), w / 2 - 27, h / 2 - 13, -16711732, false);
        }
    }
}
