package net.yezon.theabyss.client.audio;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.sounds.SoundSource;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.ShadowVeilClient;
import net.yezon.theabyss.mixin.accessor.client.SoundEngineAccessor;
import net.yezon.theabyss.mixin.accessor.client.SoundManagerAccessor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class VeilMuffle {
    private static final Set<Integer> TOUCHED = ConcurrentHashMap.newKeySet();
    private static volatile float applied;
    private static volatile int strongFilter;
    private static volatile int softFilter;
    private static volatile boolean ready;
    private static boolean unreachable;

    private VeilMuffle() {
    }

    private static boolean clear(SoundInstance sound) {
        SoundSource category = sound.getSource();
        return category == SoundSource.HOSTILE || category == SoundSource.NEUTRAL || UiSounds.isUi(sound);
    }

    private static boolean strong(SoundSource category) {
        return category == SoundSource.MUSIC || category == SoundSource.AMBIENT || category == SoundSource.RECORDS;
    }

    private static int filterFor(SoundSource category) {
        return strong(category) ? strongFilter : softFilter;
    }

    static void register(int source, SoundInstance sound) {
        if (!ready) {
            return;
        }
        if (clear(sound) || applied <= 0.001F) {
            if (TOUCHED.remove(source)) {
                AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
            }
            return;
        }
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filterFor(sound.getSource()));
        TOUCHED.add(source);
    }

    private static boolean prepare() {
        if (ready) {
            return true;
        }
        if (!AbyssReverb.available()) {
            return false;
        }
        try {
            strongFilter = EXTEfx.alGenFilters();
            EXTEfx.alFilteri(strongFilter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            softFilter = EXTEfx.alGenFilters();
            EXTEfx.alFilteri(softFilter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            ready = true;
        } catch (RuntimeException | LinkageError e) {
            TheabyssMod.LOGGER.warn("[VeilMuffle] could not create the low-pass filters", e);
        }
        return ready;
    }

    private static @Nullable Map<SoundInstance, ChannelAccess.ChannelHandle> channels() {
        if (unreachable) {
            return null;
        }
        try {
            Object engine = ((SoundManagerAccessor)Minecraft.getInstance().getSoundManager()).theabyss$soundEngine();
            return ((SoundEngineAccessor)engine).theabyss$instanceToChannel();
        } catch (RuntimeException | LinkageError e) {
            unreachable = true;
            TheabyssMod.LOGGER.warn("[VeilMuffle] cannot reach the playing sounds, muffle disabled", e);
            return null;
        }
    }

    @SubscribeEvent
    public static void onEngineLoad(SoundEngineLoadEvent event) {
        ready = false;
        applied = 0.0F;
        TOUCHED.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        float target = ShadowVeilClient.strength();
        boolean wasOn = applied > 0.001F;
        if ((!wasOn && target <= 0.001F) || !prepare()) {
            return;
        }
        if (Math.abs(target - applied) >= 0.001F) {
            applied = target;
            EXTEfx.alFilterf(strongFilter, EXTEfx.AL_LOWPASS_GAIN, 1.0F - 0.85F * target);
            EXTEfx.alFilterf(strongFilter, EXTEfx.AL_LOWPASS_GAINHF, Math.max(0.005F, 1.0F - 0.995F * target));
            EXTEfx.alFilterf(softFilter, EXTEfx.AL_LOWPASS_GAIN, 1.0F - 0.4F * target);
            EXTEfx.alFilterf(softFilter, EXTEfx.AL_LOWPASS_GAINHF, Math.max(0.03F, 1.0F - 0.97F * target));
        }
        boolean on = applied > 0.001F;
        Map<SoundInstance, ChannelAccess.ChannelHandle> channels = channels();
        if (channels == null) {
            return;
        }
        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : new ArrayList<>(channels.entrySet())) {
            SoundInstance sound = entry.getKey();
            if (clear(sound)) {
                continue;
            }
            int filter = on ? filterFor(sound.getSource()) : EXTEfx.AL_FILTER_NULL;
            entry.getValue().execute(channel -> {
                int source = AbyssReverb.source(channel);
                if (source != 0) {
                    AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filter);
                    if (filter == EXTEfx.AL_FILTER_NULL) {
                        TOUCHED.remove(source);
                    } else {
                        TOUCHED.add(source);
                    }
                }
            });
        }
    }
}
