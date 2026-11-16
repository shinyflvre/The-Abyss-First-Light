package net.yezon.theabyss.client.skill;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.RenderHandEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FxClock;
import net.yezon.theabyss.client.fx.TeleportDashClient;
import net.yezon.theabyss.client.fx.spell.SpellFx;
import net.yezon.theabyss.client.fx.spell.WraithFx;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class WraithClient {
    private static final float IN_SECONDS = 0.35F;
    private static final float OUT_SECONDS = 0.5F;
    private static final float PULSE_SECONDS = 0.7F;
    private static final float SHADOW_EYE = 0.6F;
    private static final float FOV_WIDEN = 0.07F;
    private static final float SWAY_DEGREES = 1.2F;
    private static final int HEARTBEAT_TICKS = 26;
    private static final Map<Integer, WraithFx> ACTIVE = new HashMap<>();
    private static boolean self;
    private static float intensity;
    private static float pulse;
    private static long lastNanos;
    private static boolean worldFovPending;
    private static int beat;

    private WraithClient() {
    }

    public static void set(int entityId, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        WraithFx fx = ACTIVE.get(entityId);
        if (ticks > 0) {
            if (fx != null && !fx.done()) {
                fx.extend(ticks);
            } else {
                fx = new WraithFx(entityId, ticks);
                ACTIVE.put(entityId, fx);
                SpellFx.add(fx);
            }
        } else if (fx != null) {
            fx.release();
            ACTIVE.remove(entityId);
        }
        if (mc.player != null && mc.player.getId() == entityId) {
            if (ticks > 0 && !self) {
                pulse = 1.0F;
                beat = HEARTBEAT_TICKS / 2;
            }
            self = ticks > 0;
        }
    }

    public static boolean selfActive() {
        return self;
    }

    public static float intensity() {
        return intensity;
    }

    public static float pulse() {
        return pulse;
    }

    private static float sink() {
        return intensity * intensity * (3.0F - 2.0F * intensity);
    }

    private static boolean camera(Minecraft mc) {
        return intensity > 0.0F && mc.player != null && mc.getCameraEntity() == mc.player && mc.options.getCameraType().isFirstPerson();
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        long now = FxClock.nanos();
        float dt = lastNanos == 0L ? 0.0F : Mth.clamp((now - lastNanos) / 1.0E9F, 0.0F, 0.1F);
        lastNanos = now;
        Minecraft mc = Minecraft.getInstance();
        if (self && (mc.player == null || !mc.player.isAlive())) {
            self = false;
        }
        intensity = self ? Math.min(1.0F, intensity + dt / IN_SECONDS) : Math.max(0.0F, intensity - dt / OUT_SECONDS);
        pulse = Math.max(0.0F, pulse - dt / PULSE_SECONDS);
        ACTIVE.values().removeIf(WraithFx::done);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!camera(mc)) {
            return;
        }
        worldFovPending = true;
        float time = FxClock.nanos() / 1.0E9F;
        event.setRoll(event.getRoll() + SWAY_DEGREES * sink() * Mth.sin(time * 1.7F));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (!camera(mc)) {
            return;
        }
        float k = sink();
        if (worldFovPending) {
            worldFovPending = false;
            Camera camera = event.getCamera();
            double feet = mc.player.getPosition(event.getPartialTick()).y;
            double drop = Math.min(0.0, feet + SHADOW_EYE - camera.position().y) * k;
            TeleportDashClient.moveCamera(camera, camera.position().add(0.0, drop, 0.0));
        }
        event.setFOV(event.getFOV() * (1.0F + FOV_WIDEN * k));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!self || player == null || mc.isPaused()) {
            return;
        }
        if (++beat >= HEARTBEAT_TICKS) {
            beat = 0;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT, 0.6F, 0.4F));
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (self) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        self = false;
        intensity = 0.0F;
        pulse = 0.0F;
    }
}
