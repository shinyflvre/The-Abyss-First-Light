package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.player.LocalPlayer;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.TheAbyssScreenHandlerEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class JoiningWorldOverlay {
    private JoiningWorldOverlay() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.getScreen() instanceof ProgressScreen && player != null && TheAbyssScreenHandlerEvents.execute(player)) {
            TheAbyssScreenOverlay.drawEnteringScreen(
                event.getGuiGraphics(), event.getScreen().width, event.getScreen().height, "gui.theabyss.joining_world.label_entering_the_abyss"
            );
        }
    }
}
