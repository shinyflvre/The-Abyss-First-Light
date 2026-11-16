package net.yezon.theabyss.possession.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.possession.PossessionData;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PossessionHud {
    private static Entity swappedOut;

    private PossessionHud() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiPre(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Entity camera = mc.getCameraEntity();
        if (mc.player == null || camera == null || camera == mc.player) {
            return;
        }
        if (PossessionData.of(mc.player).active() || PossessionCameraFlight.flying()) {
            swappedOut = camera;
            mc.gameRenderer.mainCamera().setEntity(mc.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiPost(RenderGuiEvent.Post event) {
        restore();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        restore();
    }

    private static void restore() {
        if (swappedOut != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getCameraEntity() == mc.player) {
                mc.gameRenderer.mainCamera().setEntity(swappedOut);
            }
            swappedOut = null;
        }
    }
}
