package net.yezon.theabyss.timestop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TimeStopClient {
    public static final float MAX_RADIUS = 320.0F;
    private static final float SPREAD_SECONDS = 2.2F;
    private static final float RESUME_SECONDS = 1.5F;
    private static final float FROZEN_FADE_SECONDS = 0.5F;
    private static final float SPREAD_CURVE = 2.2F;

    private enum Phase {
        NONE,
        SPREADING,
        STOPPED,
        RESUMING
    }

    private static Phase phase = Phase.NONE;
    private static float clock;
    private static long lastNanos;
    private static ResourceKey<Level> dimension;
    private static Vec3 centre = Vec3.ZERO;
    private static float frozen;

    private static float radius;
    private static float strength;
    private static float front;
    private static float shock;

    private TimeStopClient() {
    }

    public static float radius() {
        return radius;
    }

    public static float strength() {
        return strength;
    }

    public static float front() {
        return front;
    }

    public static float shock() {
        return shock;
    }

    public static Vec3 centreRelativeTo(Vec3 camera) {
        return centre.subtract(camera);
    }

    public static boolean active() {
        return strength > 0.001F;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getNanos();
        float dt = lastNanos == 0L || mc.isPaused() ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        if (mc.player == null || mc.level == null) {
            reset();
            return;
        }
        if (mc.level.dimension() != dimension) {
            dimension = mc.level.dimension();
            reset();
        }

        boolean casting = mc.player.hasEffect(TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate());
        if (casting && (phase == Phase.NONE || phase == Phase.RESUMING)) {
            phase = Phase.SPREADING;
            clock = 0.0F;
            centre = mc.player.position().add(0.0, mc.player.getBbHeight() * 0.5, 0.0);
        } else if (!casting && (phase == Phase.SPREADING || phase == Phase.STOPPED)) {
            phase = Phase.RESUMING;
            clock = 0.0F;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 1.6F, 0.45F));
        }
        clock += dt;
        frozen = Mth.approach(frozen, mc.player.hasEffect(TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate()) ? 1.0F : 0.0F, dt / FROZEN_FADE_SECONDS);

        switch (phase) {
            case SPREADING -> {
                float k = Mth.clamp(clock / SPREAD_SECONDS, 0.0F, 1.0F);
                radius = MAX_RADIUS * (float)Math.pow(k, SPREAD_CURVE);
                front = 1.0F - smoothstep(0.75F, 1.0F, k);
                shock = (float)Math.exp(-clock * 5.0F);
                strength = 1.0F;
                if (k >= 1.0F) {
                    phase = Phase.STOPPED;
                }
            }
            case STOPPED -> {
                radius = MAX_RADIUS;
                front = 0.0F;
                shock = 0.0F;
                strength = 1.0F;
            }
            case RESUMING -> {
                float k = Mth.clamp(clock / RESUME_SECONDS, 0.0F, 1.0F);
                radius = MAX_RADIUS * (float)Math.pow(1.0F - k, SPREAD_CURVE);
                front = smoothstep(0.0F, 0.1F, k) * (1.0F - smoothstep(0.8F, 1.0F, k));
                shock = 0.0F;
                strength = 1.0F;
                if (k >= 1.0F) {
                    phase = Phase.NONE;
                }
            }
            case NONE -> {
                radius = 0.0F;
                front = 0.0F;
                shock = 0.0F;
                strength = 0.0F;
            }
        }
        if (phase == Phase.NONE && frozen > 0.0F) {
            centre = mc.player.position();
            radius = MAX_RADIUS;
            strength = frozen;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (phase == Phase.SPREADING && clock < 0.6F) {
            event.setFOV(event.getFOV() * (1.0F - 0.06F * Mth.sin(Mth.PI * clock / 0.6F)));
        } else if (phase == Phase.RESUMING && clock < 0.5F) {
            event.setFOV(event.getFOV() * (1.0F + 0.035F * Mth.sin(Mth.PI * clock / 0.5F)));
        }
    }

    private static void reset() {
        phase = Phase.NONE;
        frozen = 0.0F;
        radius = 0.0F;
        strength = 0.0F;
        front = 0.0F;
        shock = 0.0F;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
