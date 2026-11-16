package net.yezon.theabyss.client.fx;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AfterLifeClient {
    public static final int VALUES = 15;
    private static final int WAVES = 4;
    private static final float WAVE_SPEED = 64.0F;
    private static final float WAVE_TAIL = 48.0F;
    private static final float DUB_GAP = 0.24F;
    private static final float DUB_STRENGTH = 0.7F;
    private static final float CALM_PERIOD = 1.3F;
    private static final float PANIC_PERIOD = 0.55F;
    private static final float FADE_IN = 1.4F;
    private static final float FADE_OUT = 0.6F;
    private static final float DEATH_FLASH = 1.2F;
    private static final float RESTORE_SECONDS = 2.2F;
    private static final float RESTORE_SPEED = 38.0F;
    private static final float BEAT_DECAY = 7.0F;
    private static final double SOUND_DISTANCE = 5.0;

    private static boolean active;
    private static Vec3 body = Vec3.ZERO;
    private static ResourceKey<Level> dimension;
    private static long startNanos;
    private static float total;
    private static float foundAt = -1.0F;
    private static float endAt = -1.0F;
    private static float nextBeat;
    private static float beatFlash;
    private static float lastTime;
    private static final float[] waveBirth = new float[WAVES];
    private static final float[] waveStrength = new float[WAVES];
    private static final boolean[] wavePassed = new boolean[WAVES];

    private AfterLifeClient() {
    }

    public static void start(Vec3 bodyAt, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        active = true;
        body = bodyAt;
        dimension = mc.level != null ? mc.level.dimension() : null;
        startNanos = FxClock.nanos();
        total = Math.max(1.0F, ticks / 20.0F);
        foundAt = -1.0F;
        endAt = -1.0F;
        nextBeat = 0.6F;
        beatFlash = 0.0F;
        lastTime = 0.0F;
        Arrays.fill(waveBirth, Float.NaN);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SOUL_ESCAPE.value(), 0.5F, 1.0F));
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 0.45F, 0.8F));
    }

    public static void found() {
        if (!active) {
            return;
        }
        foundAt = time();
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_ACTIVATE, 1.4F, 0.8F));
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.9F, 1.0F));
    }

    public static void end() {
        if (active && endAt < 0.0F) {
            endAt = time();
        }
    }

    public static boolean active() {
        return active;
    }

    private static float time() {
        return (FxClock.nanos() - startNanos) / 1.0E9F;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (!active) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.level.dimension() != dimension) {
            active = false;
            return;
        }
        float t = time();
        float dt = Math.max(0.0F, t - lastTime);
        lastTime = t;
        if (endAt >= 0.0F && t - endAt > FADE_OUT || foundAt >= 0.0F && t - foundAt > RESTORE_SECONDS) {
            active = false;
            return;
        }
        beatFlash *= (float)Math.exp(-BEAT_DECAY * dt);
        if (foundAt < 0.0F && endAt < 0.0F) {
            while (t >= nextBeat) {
                spawnWave(nextBeat, 1.0F);
                spawnWave(nextBeat + DUB_GAP, DUB_STRENGTH);
                float urgency = Mth.clamp(nextBeat / total, 0.0F, 1.0F);
                nextBeat += Mth.lerp((float)Math.pow(urgency, 1.2), CALM_PERIOD, PANIC_PERIOD);
            }
        }
        Vec3 eye = mc.player.getEyePosition();
        double distance = eye.distanceTo(body);
        for (int i = 0; i < WAVES; i++) {
            if (Float.isNaN(waveBirth[i])) {
                continue;
            }
            float radius = (t - waveBirth[i]) * WAVE_SPEED;
            if (!wavePassed[i] && radius >= distance) {
                wavePassed[i] = true;
                heartbeat(mc, eye, waveStrength[i]);
            }
            if (radius > distance + WAVE_TAIL) {
                waveBirth[i] = Float.NaN;
            }
        }
    }

    private static void spawnWave(float birth, float strength) {
        int slot = 0;
        for (int i = 0; i < WAVES; i++) {
            if (Float.isNaN(waveBirth[i])) {
                slot = i;
                break;
            }
            if (waveBirth[i] < waveBirth[slot]) {
                slot = i;
            }
        }
        waveBirth[slot] = birth;
        waveStrength[slot] = strength;
        wavePassed[slot] = false;
    }

    private static void heartbeat(Minecraft mc, Vec3 eye, float strength) {
        Vec3 towards = body.subtract(eye);
        Vec3 at = towards.lengthSqr() > 1.0E-4 ? eye.add(towards.normalize().scale(SOUND_DISTANCE)) : eye;
        float urgency = Mth.clamp(lastTime / total, 0.0F, 1.0F);
        float volume = (0.75F + 0.5F * urgency) * strength;
        float pitch = strength >= 1.0F ? 0.95F + 0.15F * urgency : 1.15F + 0.15F * urgency;
        SoundEvent beat = SoundEvents.WARDEN_HEARTBEAT;
        mc.getSoundManager().play(new SimpleSoundInstance(beat, SoundSource.PLAYERS, volume, pitch, SoundInstance.createUnseededRandom(), at.x, at.y, at.z));
        beatFlash = Math.max(beatFlash, strength);
    }

    public static void fill(Vec3 camera, float[] out) {
        Arrays.fill(out, 0.0F);
        if (!active) {
            out[4] = -1.0F;
            for (int i = 0; i < WAVES; i++) {
                out[5 + i * 2] = -1.0F;
            }
            return;
        }
        float t = time();
        out[0] = (float)(body.x - camera.x);
        out[1] = (float)(body.y - camera.y);
        out[2] = (float)(body.z - camera.z);
        float strength = Mth.clamp(t / FADE_IN, 0.0F, 1.0F);
        strength = strength * strength * (3.0F - 2.0F * strength);
        if (endAt >= 0.0F) {
            strength *= 1.0F - Mth.clamp((t - endAt) / FADE_OUT, 0.0F, 1.0F);
        }
        if (foundAt >= 0.0F) {
            float k = Mth.clamp((t - foundAt) / RESTORE_SECONDS, 0.0F, 1.0F);
            strength *= 1.0F - k * k;
        }
        out[3] = strength;
        out[4] = foundAt >= 0.0F ? (t - foundAt) * RESTORE_SPEED : -1.0F;
        for (int i = 0; i < WAVES; i++) {
            boolean live = !Float.isNaN(waveBirth[i]) && t >= waveBirth[i];
            out[5 + i * 2] = live ? (t - waveBirth[i]) * WAVE_SPEED : -1.0F;
            out[6 + i * 2] = live ? waveStrength[i] : 0.0F;
        }
        out[13] = beatFlash;
        out[14] = 1.0F - Mth.clamp(t / DEATH_FLASH, 0.0F, 1.0F);
    }
}
