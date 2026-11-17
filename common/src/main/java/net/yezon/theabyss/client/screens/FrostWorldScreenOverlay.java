package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.IntroScreenHandlerEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class FrostWorldScreenOverlay {
    private FrostWorldScreenOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (TheAbyssScreenOverlay.levelLoading(event.getScreen()) && player != null && IntroScreenHandlerEvents.execute(player)) {
            GuiGraphics graphics = event.getGuiGraphics();
            int w = event.getScreen().width;
            int h = event.getScreen().height;
            graphics.fill(0, 0, w, h, TheAbyssScreenOverlay.LOADING_COLOUR);
            GuiGraphicsCompat.text(
                graphics, Minecraft.getInstance().font, Component.translatable("gui.theabyss.frost_world_screen.label_loading"), w / 2 - 27, h / 2 - 13, -16711732, false
            );
        }
    }
}
