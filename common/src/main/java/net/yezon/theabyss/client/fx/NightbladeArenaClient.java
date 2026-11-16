package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeArenaClient {
    private static final float GROW_SECONDS = 0.6F;
    private static final float DISSOLVE_SECONDS = 1.0F;

    private static Vec3 centre = Vec3.ZERO;
    private static float radius;
    private static boolean red;
    private static long startNanos;
    private static long endNanos = Long.MIN_VALUE;
    private static Level level;

    private NightbladeArenaClient() {
    }

    public static void set(double x, double y, double z, float r, int durationTicks, boolean isRed) {
        long now = FxClock.nanos();
        if (durationTicks <= 0) {
            endNanos = Math.min(endNanos, now);
            return;
        }
        centre = new Vec3(x, y, z);
        radius = r;
        red = isRed;
        startNanos = now;
        endNanos = now + durationTicks * 50_000_000L;
    }

    public static boolean active() {
        return endNanos != Long.MIN_VALUE && FxClock.nanos() < endNanos + (long)(DISSOLVE_SECONDS * 1.0E9F);
    }

    private static float currentRadius() {
        long now = FxClock.nanos();
        float age = (now - startNanos) / 1.0E9F;
        float grow = Mth.clamp(age / GROW_SECONDS, 0.0F, 1.0F);
        grow = 1.0F - (1.0F - grow) * (1.0F - grow);
        float dissolve = now < endNanos ? 1.0F : Mth.clamp(1.0F - (now - endNanos) / 1.0E9F / DISSOLVE_SECONDS, 0.0F, 1.0F);
        return radius * grow * dissolve;
    }

    public static void fill(Vec3 camera, float[] out) {
        if (!active()) {
            out[3] = 0.0F;
            return;
        }
        out[0] = (float)(centre.x - camera.x);
        out[1] = (float)(centre.y - camera.y);
        out[2] = (float)(centre.z - camera.z);
        out[3] = currentRadius();
        out[4] = red ? 1.0F : 0.0F;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            level = mc.level;
            endNanos = Long.MIN_VALUE;
        }
    }
}
