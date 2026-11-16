package net.yezon.theabyss.client.audio;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.SelectMusicEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.init.TheabyssModSounds;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeMusic {
    private static final double RANGE = 64.0;
    private static final int GRACE_TICKS = 80;
    private static final int FADE_IN_TICKS = 30;
    private static final int CROSSFADE_TICKS = 50;
    private static final int FADE_OUT_TICKS = 80;

    private static final List<Track> TRACKS = new ArrayList<>();
    private static Track current;
    private static int phase;
    private static int missing;
    private static int restartCooldown;

    private NightbladeMusic() {
    }

    private static final class Track extends AbstractTickableSoundInstance {
        private float target = 1.0F;
        private float step;

        Track(SoundEvent event, int fadeTicks) {
            super(event, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.001F;
            this.step = 1.0F / fadeTicks;
        }

        void fadeOut(int ticks) {
            this.target = 0.0F;
            this.step = Math.max(this.volume, 0.01F) / ticks;
        }

        @Override
        public void tick() {
            if (this.volume < this.target) {
                this.volume = Math.min(this.target, this.volume + this.step);
            } else if (this.volume > this.target) {
                this.volume = Math.max(this.target, this.volume - this.step);
            }
            if (this.target <= 0.0F && this.volume <= 0.0F) {
                this.stop();
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        int wanted = 0;
        if (player != null && mc.level != null) {
            for (NightbladeBossEntity boss : mc.level.getEntitiesOfClass(NightbladeBossEntity.class, player.getBoundingBox().inflate(RANGE))) {
                if (!boss.isMirrorSynced() && boss.isAlive()) {
                    wanted = Math.max(wanted, Math.max(1, Math.min(3, boss.getPhase())));
                }
            }
        }
        if (wanted == 0 && phase != 0 && player != null && ++missing < GRACE_TICKS) {
            wanted = phase;
        } else if (wanted != 0) {
            missing = 0;
        }
        if (wanted != phase) {
            switchTo(mc, wanted);
        }
        TRACKS.removeIf(track -> track.isStopped() || !mc.getSoundManager().isActive(track) && track != current);
        if (restartCooldown > 0) {
            restartCooldown--;
        } else if (current != null && !mc.getSoundManager().isActive(current)) {
            restartCooldown = 100;
            current = play(mc, phase, FADE_IN_TICKS);
        }
    }

    private static void switchTo(Minecraft mc, int next) {
        int fade = next == 0 ? FADE_OUT_TICKS : CROSSFADE_TICKS;
        boolean fromSilence = current == null;
        if (current != null) {
            current.fadeOut(fade);
        }
        phase = next;
        missing = 0;
        restartCooldown = 100;
        current = next == 0 ? null : play(mc, next, fromSilence ? FADE_IN_TICKS : fade);
        if (current != null) {
            mc.getMusicManager().stopPlaying();
        }
    }

    private static Track play(Minecraft mc, int phase, int fadeTicks) {
        SoundEvent event = switch (phase) {
            case 2 -> TheabyssModSounds.NIGHTBLADE_PHASE_2.get();
            case 3 -> TheabyssModSounds.NIGHTBLADE_PHASE_3.get();
            default -> TheabyssModSounds.NIGHTBLADE_PHASE_1.get();
        };
        Track track = new Track(event, fadeTicks);
        TRACKS.add(track);
        mc.getSoundManager().play(track);
        return track;
    }

    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        if (current != null || !TRACKS.isEmpty()) {
            event.setMusic(null);
        }
    }
}
