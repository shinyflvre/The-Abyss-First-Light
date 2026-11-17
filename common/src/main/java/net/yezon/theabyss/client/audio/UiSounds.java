package net.yezon.theabyss.client.audio;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.jspecify.annotations.Nullable;

public final class UiSounds {
    private static final Set<SoundInstance> MARKED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private UiSounds() {
    }

    public static <T extends SoundInstance> T mark(T sound) {
        if (sound != null) {
            MARKED.add(sound);
        }
        return sound;
    }

    public static boolean isUi(@Nullable SoundInstance sound) {
        return sound != null && MARKED.contains(sound);
    }

    public static <T extends SoundInstance> T carry(@Nullable SoundInstance from, T to) {
        return isUi(from) ? mark(to) : to;
    }
}
