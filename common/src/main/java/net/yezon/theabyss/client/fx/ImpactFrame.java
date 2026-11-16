package net.yezon.theabyss.client.fx;

import net.minecraft.util.Util;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ImpactFrame {
    private static final float INVERTED_SECONDS = 0.05F;
    private static final float STARK_SECONDS = 0.11F;
    private static final float FADE_SECONDS = 0.32F;
    private static final float SHAKE_SECONDS = 0.35F;
    private static long startNanos = -1L;
    private static boolean parry;

    private ImpactFrame() {
    }

    public static void start(boolean parried) {
        startNanos = Util.getNanos();
        parry = parried;
        CameraShake.add(parried ? 1.4F : 2.2F, SHAKE_SECONDS);
    }

    public static float tint() {
        return parry ? 1.0F : 0.0F;
    }

    private static float seconds() {
        return startNanos < 0L ? Float.MAX_VALUE : (Util.getNanos() - startNanos) / 1.0E9F;
    }

    public static float strength() {
        float t = seconds();
        if (t < INVERTED_SECONDS) {
            return 1.0F;
        }
        if (t < STARK_SECONDS) {
            return 0.7F;
        }
        if (t < FADE_SECONDS) {
            return 0.7F * (1.0F - (t - STARK_SECONDS) / (FADE_SECONDS - STARK_SECONDS));
        }
        return 0.0F;
    }
}
