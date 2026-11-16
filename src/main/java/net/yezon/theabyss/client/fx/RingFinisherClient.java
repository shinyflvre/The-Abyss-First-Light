package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;

public final class RingFinisherClient {
    private static final Identifier SOUND = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "ring_finisher");
    private static final RandomSource RANDOM = RandomSource.create();
    private static final long SAME_FINISHER_MS = 250L;
    private static long lastPlayed;

    private RingFinisherClient() {
    }

    public static void play() {
        Minecraft mc = Minecraft.getInstance();
        TheabyssMod.LOGGER.info("[RingFinisher] client received, config {}", ConfigConfiguration.RESONANT_LIKE_AUDIO.get());
        if (mc.player == null || !ConfigConfiguration.RESONANT_LIKE_AUDIO.get()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastPlayed < SAME_FINISHER_MS) {
            return;
        }
        lastPlayed = now;
        float pitch = 0.9F + RANDOM.nextFloat() * 0.2F;
        SoundInstance sound = new SimpleSoundInstance(SOUND, SoundSource.PLAYERS, 1.0F, pitch, RANDOM, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0,
            0.0, true);
        var result = mc.getSoundManager().play(sound);
        TheabyssMod.LOGGER.info("[RingFinisher] client play {} pitch {}", result, pitch);
    }
}
