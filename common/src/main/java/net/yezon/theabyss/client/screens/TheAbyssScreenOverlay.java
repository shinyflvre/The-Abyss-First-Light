package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.TheAbyssScreenHandlerEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TheAbyssScreenOverlay {
    static final int LOADING_COLOUR = 0xFF0C1E22;
    static final int BLACK_COLOUR = 0xFF000000;

    private TheAbyssScreenOverlay() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.getScreen() instanceof LevelLoadingScreen && player != null && TheAbyssScreenHandlerEvents.execute(player)) {
            drawEnteringScreen(event.getGuiGraphics(), event.getScreen().width, event.getScreen().height, "gui.theabyss.the_abyss_screen.label_entering_the_abyss");
        }
    }

    static void drawEnteringScreen(GuiGraphicsExtractor graphics, int w, int h, String labelKey) {
        graphics.fill(0, 0, w, h, BLACK_COLOUR);
        graphics.text(Minecraft.getInstance().font, Component.translatable(labelKey), w / 2 - 52, h / 2 - 13, -16711732, false);
    }
}
