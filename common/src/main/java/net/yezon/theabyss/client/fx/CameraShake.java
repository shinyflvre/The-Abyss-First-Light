package net.yezon.theabyss.client.fx;

import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class CameraShake {
    private static long startNanos = -1L;
    private static float strength;
    private static float seconds = 1.0F;

    private CameraShake() {
    }

    public static void add(float degrees, float duration) {
        if (degrees >= current()) {
            startNanos = Util.getNanos();
            strength = degrees;
            seconds = Math.max(0.05F, duration);
        }
    }

    private static float current() {
        if (startNanos < 0L) {
            return 0.0F;
        }
        float t = (Util.getNanos() - startNanos) / 1.0E9F;
        return t >= seconds ? 0.0F : strength * Mth.square(1.0F - t / seconds);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float amount = current();
        if (amount <= 0.0F) {
            return;
        }
        float time = (Util.getNanos() - startNanos) / 1.0E9F * 60.0F;
        event.setYaw(event.getYaw() + Mth.sin(time * 1.7F) * amount);
        event.setPitch(event.getPitch() + Mth.sin(time * 2.3F + 1.0F) * amount);
        event.setRoll(event.getRoll() + Mth.sin(time * 1.3F + 2.0F) * amount * 1.4F);
    }
}
