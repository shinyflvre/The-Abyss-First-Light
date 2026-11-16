package net.yezon.theabyss.client.fx;

import java.lang.reflect.Method;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TeleportDashClient {
    private static final float SECONDS = 0.4F;
    private static final float FOV_STRETCH = 0.24F;
    private static final float ROLL_DEGREES = 5.0F;

    private static boolean active;
    private static Level level;
    private static Vec3 from;
    private static float fromYaw;
    private static float fromPitch;
    private static float rollSign;
    private static boolean worldFovPending;
    private static long startNanos;

    private static Method setPosition;
    private static boolean reflectionFailed;

    private TeleportDashClient() {
    }

    public static void start(Vec3 fromEye, float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getCameraEntity() != mc.player || reflectionFailed) {
            return;
        }
        level = mc.level;
        from = fromEye;
        fromYaw = yaw;
        fromPitch = pitch;
        rollSign = 0.0F;
        startNanos = Util.getNanos();
        active = true;
    }

    public static boolean active() {
        return active;
    }

    private static float progress() {
        return Mth.clamp((Util.getNanos() - startNanos) / 1.0E9F / SECONDS, 0.0F, 1.0F);
    }

    private static float rush(float t) {
        float k = 1.0F - t;
        return 1.0F - k * k * k;
    }

    private static float speed(float t) {
        float k = 1.0F - t;
        return k * k;
    }

    private static boolean running(Minecraft mc) {
        if (!active) {
            return false;
        }
        if (mc.player == null || mc.level != level || mc.getCameraEntity() != mc.player || !mc.player.isAlive() || progress() >= 1.0F) {
            active = false;
            return false;
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!running(mc)) {
            return;
        }
        worldFovPending = true;
        float t = progress();
        float turn = Mth.clamp(t * 1.6F, 0.0F, 1.0F);
        turn = turn * turn * (3.0F - 2.0F * turn);
        event.setYaw(Mth.rotLerp(turn, fromYaw, event.getYaw()));
        event.setPitch(Mth.lerp(turn, fromPitch, event.getPitch()));
        if (rollSign == 0.0F) {
            Vec3 way = mc.player.getEyePosition().subtract(from);
            Vec3 look = Vec3.directionFromRotation(fromPitch, fromYaw);
            rollSign = look.x * way.z - look.z * way.x >= 0.0 ? 1.0F : -1.0F;
        }
        event.setRoll(event.getRoll() + rollSign * ROLL_DEGREES * Mth.sin(t * Mth.PI) * speed(t));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (!running(mc)) {
            return;
        }
        float t = progress();
        if (worldFovPending) {
            worldFovPending = false;
            Camera camera = event.getCamera();
            Vec3 eye = mc.player.getEyePosition(event.getPartialTick());
            Vec3 back = from.subtract(eye).scale(1.0F - rush(t));
            moveCamera(camera, camera.position().add(back));
        }
        float stretch = Mth.sin(Math.min(t * 3.0F, 1.0F) * Mth.HALF_PI) * (1.0F - t);
        event.setFOV(event.getFOV() * (1.0F + FOV_STRETCH * stretch));
    }

    public static boolean moveCamera(Camera camera, Vec3 position) {
        if (reflectionFailed) {
            return false;
        }
        try {
            if (setPosition == null) {
                setPosition = Camera.class.getDeclaredMethod("setPosition", Vec3.class);
                setPosition.setAccessible(true);
            }
            setPosition.invoke(camera, position);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            reflectionFailed = true;
            active = false;
            TheabyssMod.LOGGER.warn("Teleport rush: camera position not settable, rush disabled", e);
            return false;
        }
    }
}
