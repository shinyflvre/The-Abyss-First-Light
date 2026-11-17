package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.TheAbyssScreenHandlerEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class JoiningScreenOverlay {
    private JoiningScreenOverlay() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (TheAbyssScreenOverlay.levelLoading(event.getScreen()) && Minecraft.getInstance().player == null && TheAbyssScreenHandlerEvents.execute(null)) {
            TheAbyssScreenOverlay.drawEnteringScreen(
                event.getGuiGraphics(), event.getScreen().width, event.getScreen().height, "gui.theabyss.joining_screen.label_entering_the_abyss"
            );
        }
    }
}
