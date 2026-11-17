package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FxClock;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RewindCamera {
    private static final float SECONDS = 0.75F;
    private static final float FOV_SQUEEZE = 0.14F;
    private static final float ROLL_DEGREES = 6.0F;

    private static Vec3[] path;
    private static float fromYaw;
    private static float fromPitch;
    private static long startNanos;
    private static Level level;
    private static boolean active;
    private static boolean worldFovPending;
    private static boolean reflectionFailed;

    private RewindCamera() {
    }

    static void start(Vec3[] eyes, float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getCameraEntity() != mc.player || reflectionFailed || eyes.length < 2) {
            return;
        }
        path = eyes;
        fromYaw = yaw;
        fromPitch = pitch;
        startNanos = FxClock.nanos();
        level = mc.level;
        active = true;
    }

    private static float progress() {
        return Mth.clamp((FxClock.nanos() - startNanos) / 1.0E9F / SECONDS, 0.0F, 1.0F);
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

    private static Vec3 along(float s) {
        float index = s * (path.length - 1);
        int i = Math.min(path.length - 2, (int)index);
        return path[i].lerp(path[i + 1], index - i);
    }

    private static float ease(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!running(mc)) {
            return;
        }
        worldFovPending = true;
        float t = progress();
        float turn = ease(Mth.clamp(t * 1.3F, 0.0F, 1.0F));
        event.setYaw(Mth.rotLerp(turn, fromYaw, event.getYaw()));
        event.setPitch(Mth.lerp(turn, fromPitch, event.getPitch()));
        event.setRoll(event.getRoll() + ROLL_DEGREES * Mth.sin(t * Mth.PI * 2.0F) * (1.0F - t));
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
            Vec3 offset = along(ease(t)).subtract(eye).scale(1.0F - ease(Mth.clamp((t - 0.85F) / 0.15F, 0.0F, 1.0F)));
            move(camera, camera.getPosition().add(offset));
        }
        float squeeze = Mth.sin(t * Mth.PI);
        event.setFOV(event.getFOV() * (1.0F - FOV_SQUEEZE * squeeze));
    }

    private static final net.minecraft.resources.ResourceLocation RING = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/parry_ring.png");

    public static void renderTunnel(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        if (active) {
            float t = progress();
            drawTunnel(g, t, Mth.sin(t * Mth.PI));
        } else if (net.yezon.theabyss.client.fx.ClockRewindClient.tunnel()) {
            drawTunnel(g, net.yezon.theabyss.client.fx.ClockRewindClient.tunnelTime(), net.yezon.theabyss.client.fx.ClockRewindClient.tunnelStrength());
        }
    }

    private static void drawTunnel(net.minecraft.client.gui.GuiGraphics g, float t, float strength) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        int cx = w / 2;
        int cy = h / 2;
        g.fill(0, 0, w, h, net.yezon.theabyss.compat.ARGB.color(Math.round(70 * strength), 40, 150, 170));
        float diagonal = (float)Math.sqrt(w * w + h * h);
        for (int k = 0; k < 4; k++) {
            float phase = (t * 3.0F + k * 0.25F) % 1.0F;
            int size = Math.round(12 + diagonal * 1.3F * phase * phase);
            int alpha = Math.round(200 * strength * (1.0F - phase));
            int colour = k % 2 == 0 ? net.yezon.theabyss.compat.ARGB.color(alpha, 127, 232, 255) : net.yezon.theabyss.compat.ARGB.color(alpha, 255, 211, 106);
            net.yezon.theabyss.compat.GuiGraphicsCompat.blit(g, RING, cx - size / 2, cy - size / 2, 0.0F, 0.0F, size, size, size, size, colour);
        }
        float angle = -t * Mth.TWO_PI * 2.0F;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0F);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotation(angle));
        g.fill(-1, -34, 1, -6, net.yezon.theabyss.compat.ARGB.color(Math.round(220 * strength), 255, 255, 255));
        g.pose().popPose();
    }

    private static void move(Camera camera, Vec3 position) {
        try {
            ((net.yezon.theabyss.mixin.client.CameraPositionInvoker)camera).theabyss$setPosition(position);
        } catch (RuntimeException e) {
            reflectionFailed = true;
            active = false;
            TheabyssMod.LOGGER.warn("Rewind: camera position not settable, rewind flight disabled", e);
        }
    }
}
