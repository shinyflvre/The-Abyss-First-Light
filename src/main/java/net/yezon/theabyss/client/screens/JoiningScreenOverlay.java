package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.TheAbyssScreenHandlerEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class JoiningScreenOverlay {
    private JoiningScreenOverlay() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof LevelLoadingScreen && Minecraft.getInstance().player == null && TheAbyssScreenHandlerEvents.execute(null)) {
            TheAbyssScreenOverlay.drawEnteringScreen(
                event.getGuiGraphics(), event.getScreen().width, event.getScreen().height, "gui.theabyss.joining_screen.label_entering_the_abyss"
            );
        }
    }
}
