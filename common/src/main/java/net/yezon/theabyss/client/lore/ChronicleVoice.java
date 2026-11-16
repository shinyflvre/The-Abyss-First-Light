package net.yezon.theabyss.client.lore;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ChronicleVoice {
    private static final Deque<Integer> QUEUE = new ArrayDeque<>();
    private static SoundInstance playing;

    private ChronicleVoice() {
    }

    public static boolean speaking() {
        return playing != null || !QUEUE.isEmpty();
    }

    public static Identifier sound(int page) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, String.format("chronicle.page_%02d", page));
    }

    public static void noted(int page) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.9F, 0.8F));
        QUEUE.addLast(page);
    }

    private static boolean recorded(SoundManager sounds, Identifier id) {
        WeighedSoundEvents event = sounds.getSoundEvent(id);
        return event != null && event.getSound(RandomSource.create()) != SoundManager.EMPTY_SOUND;
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            QUEUE.clear();
            playing = null;
            ChronicleSubtitles.stop();
            return;
        }
        SoundManager sounds = mc.getSoundManager();
        if (playing != null && sounds.isActive(playing)) {
            return;
        }
        if (playing != null) {
            ChronicleSubtitles.stop();
        }
        playing = null;
        while (!QUEUE.isEmpty()) {
            int page = QUEUE.pollFirst();
            Identifier id = sound(page);
            if (recorded(sounds, id)) {
                playing = new SimpleSoundInstance(id, SoundSource.VOICE, 1.0F, 1.0F, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true) {
                    @Override
                    public boolean canStartSilent() {
                        return true;
                    }
                };
                sounds.play(playing);
                ChronicleSubtitles.start(page);
                return;
            }
        }
    }
}
