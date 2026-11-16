package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class FxClock {
    private static long nanos;
    private static long lastReal;

    private FxClock() {
    }

    public static long nanos() {
        return nanos;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        long real = Util.getNanos();
        if (lastReal != 0L && !Minecraft.getInstance().isPaused()) {
            nanos += Math.min(real - lastReal, 100_000_000L);
        }
        lastReal = real;
    }
}
