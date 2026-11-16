package net.yezon.theabyss.client.fx;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class EchoVisionClient {
    private static final float FADE_IN_SECONDS = 0.7F;
    private static final float FADE_OUT_SECONDS = 0.6F;
    private static final float FLASH_SECONDS = 0.55F;
    private static final float FOV_SWELL = 0.14F;

    private static boolean active;
    private static Vec3 body = Vec3.ZERO;
    private static float range;
    private static Level level;
    private static float intensity;
    private static float flash;
    private static float edge;
    private static long lastNanos;

    private static boolean flying;
    private static Vec3 flyFrom = Vec3.ZERO;
    private static float flyYaw;
    private static float flyPitch;
    private static long flyStart;
    private static float flySeconds;
    private static boolean worldFovPending;

    private EchoVisionClient() {
    }

    public static void receive(boolean start, Vec3 fromEye, float yaw, float pitch, Vec3 bodyAt, float leash) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        active = start;
        body = bodyAt;
        range = Math.max(1.0F, leash);
        level = mc.level;
        flash = 1.0F;
        flyFrom = fromEye;
        flyYaw = yaw;
        flyPitch = pitch;
        flyStart = Util.getNanos();
        flySeconds = -1.0F;
        flying = mc.getCameraEntity() == mc.player;
    }

    public static boolean active() {
        return active;
    }

    public static float intensity() {
        return intensity;
    }

    public static float flash() {
        return flash;
    }

    public static float edge() {
        return edge;
    }

    public static float range() {
        return range;
    }

    public static @Nullable Vec3 body() {
        return active ? body : null;
    }

    public static boolean shaderOn() {
        return intensity > 0.002F || flash > 0.002F;
    }

    private static void reset() {
        active = false;
        flying = false;
        intensity = 0.0F;
        flash = 0.0F;
        edge = 0.0F;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getNanos();
        float dt = lastNanos == 0L || mc.isPaused() ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        if (mc.player == null || mc.level == null) {
            reset();
            return;
        }
        if (mc.level != level) {
            active = false;
            flying = false;
        }
        intensity = Mth.approach(intensity, active ? 1.0F : 0.0F, dt / (active ? FADE_IN_SECONDS : FADE_OUT_SECONDS));
        flash = Math.max(0.0F, flash - dt / FLASH_SECONDS);
        float wantEdge = 0.0F;
        if (active) {
            float d = (float)(mc.player.position().distanceTo(body) / range);
            float k = Mth.clamp((d - 0.65F) / 0.35F, 0.0F, 1.0F);
            wantEdge = k * k * (3.0F - 2.0F * k);
        }
        edge = Mth.approach(edge, wantEdge, dt * 3.0F);
    }

    private static float progress() {
        if (flySeconds <= 0.0F) {
            return 0.0F;
        }
        return Mth.clamp((Util.getNanos() - flyStart) / 1.0E9F / flySeconds, 0.0F, 1.0F);
    }

    private static float ease(float t) {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
    }

    private static boolean flying(Minecraft mc) {
        if (!flying) {
            return false;
        }
        if (mc.player == null || mc.level != level || mc.getCameraEntity() != mc.player || !mc.player.isAlive()
            || flySeconds > 0.0F && progress() >= 1.0F) {
            flying = false;
            return false;
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!flying(mc)) {
            return;
        }
        worldFovPending = true;
        float turn = ease(Mth.clamp(progress() * 1.15F, 0.0F, 1.0F));
        event.setYaw(Mth.rotLerp(turn, flyYaw, event.getYaw()));
        event.setPitch(Mth.lerp(turn, flyPitch, event.getPitch()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (!flying(mc)) {
            return;
        }
        Vec3 eye = mc.player.getEyePosition(event.getPartialTick());
        if (flySeconds <= 0.0F) {
            double distance = flyFrom.distanceTo(eye);
            flySeconds = (float)Mth.clamp(0.5 + distance / 80.0, 0.5, 1.6);
            flyStart = Util.getNanos();
        }
        float t = progress();
        if (worldFovPending) {
            worldFovPending = false;
            float k = ease(t);
            double arc = Math.min(6.0, flyFrom.distanceTo(eye) * 0.12) * Mth.sin(k * Mth.PI);
            Vec3 path = flyFrom.lerp(eye, k).add(0.0, arc, 0.0);
            Camera camera = event.getCamera();
            TeleportDashClient.moveCamera(camera, camera.position().add(path.subtract(eye)));
        }
        event.setFOV(event.getFOV() * (1.0F + FOV_SWELL * Mth.sin(t * Mth.PI)));
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || intensity < 0.05F || sound instanceof TickableSoundInstance || sound.isRelative()
            || sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.MASTER) {
            return;
        }
        float volume = Mth.clamp(1.0F - 0.45F * intensity, 0.1F, 1.0F);
        float pitch = 1.0F - 0.06F * intensity;
        event.setSound(new MuffledSound(sound, volume, pitch));
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
    }
}
