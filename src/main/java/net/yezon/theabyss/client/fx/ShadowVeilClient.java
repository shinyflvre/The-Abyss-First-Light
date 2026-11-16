package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.ability.ShadowVeil;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ShadowVeilClient {
    private static final float FADE = 0.15F;
    private static final float LOW = 0.25F;
    private static final int BEAT_FIRST = 8;
    private static final int BEAT = 30;
    private static final int BEAT_LOW = 20;
    private static final float BEAT_VOLUME = 0.3F;
    private static final float BEAT_PITCH = 0.85F;
    private static int nextBeat;
    private static boolean active;
    private static long startNanos;
    private static float energyAtStart;
    private static float strength;
    private static Level level;

    private ShadowVeilClient() {
    }

    public static void set(boolean on, int energy) {
        if (on != active) {
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SOUL_ESCAPE.value(), on ? 0.6F : 0.7F, 1.0F));
            nextBeat = BEAT_FIRST;
        }
        active = on;
        energyAtStart = energy;
        startNanos = FxClock.nanos();
    }

    public static float localRadius() {
        return active ? ShadowVeil.RADIUS : 0.0F;
    }

    private static float left() {
        float ticks = (FxClock.nanos() - startNanos) / 5.0E7F;
        return Math.max(0.0F, (energyAtStart - ticks) / ShadowVeil.MAX);
    }

    public static float circle() {
        float left = left();
        if (left >= LOW) {
            return 0.8F;
        }
        float pulse = 0.5F + 0.5F * (float)Math.sin(FxClock.nanos() / 1.0E9 * Math.PI * 4.0);
        return 0.35F + 0.45F * pulse;
    }

    public static float strength() {
        return strength;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            level = mc.level;
            active = false;
        }
        if (mc.player == null) {
            active = false;
        }
        if (active && !mc.isPaused() && --nextBeat <= 0) {
            nextBeat = left() < LOW ? BEAT_LOW : BEAT;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT, BEAT_PITCH, BEAT_VOLUME));
        }
        float target = active ? 1.0F : 0.0F;
        strength = strength < target ? Math.min(target, strength + FADE) : Math.max(target, strength - FADE);
    }
}
