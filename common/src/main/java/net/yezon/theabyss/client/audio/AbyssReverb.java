package net.yezon.theabyss.client.audio;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.PlayStreamingSourceEvent;
import net.yezon.theabyss.platform.client.event.sound.SoundEngineLoadEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.mixin.accessor.client.ChannelAccessor;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssReverb {
    private static volatile boolean active;
    private static volatile boolean needsSetup = true;
    private static boolean available;
    private static int slot;
    private static int effect;
    private static float appliedGain = -1.0F;
    private static float appliedDecay = -1.0F;

    private AbyssReverb() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean enabled;
        try {
            enabled = ConfigConfiguration.REVERB_EFFECT.get();
        } catch (IllegalStateException notLoadedYet) {
            enabled = false;
        }
        active = enabled && mc.level != null && mc.level.dimension().location().toString().equals("theabyss:the_abyss");
    }

    @SubscribeEvent
    public static void onEngineLoad(SoundEngineLoadEvent event) {
        needsSetup = true;
    }

    @SubscribeEvent
    public static void onSound(PlaySoundSourceEvent event) {
        handle(event.getSound(), event.getChannel());
    }

    @SubscribeEvent
    public static void onStream(PlayStreamingSourceEvent event) {
        handle(event.getSound(), event.getChannel());
    }

    private static void handle(SoundInstance sound, Channel channel) {
        if (needsSetup) {
            needsSetup = false;
            setup();
        }
        if (!available) {
            return;
        }
        int source = source(channel);
        if (source == 0) {
            return;
        }
        updateParameters();
        SoundSource category = sound.getSource();
        boolean reverb = active && !sound.isRelative() && category != SoundSource.MUSIC && category != SoundSource.AMBIENT;
        AL11.alSource3i(source, EXTEfx.AL_AUXILIARY_SEND_FILTER, reverb ? slot : EXTEfx.AL_EFFECTSLOT_NULL, 0, EXTEfx.AL_FILTER_NULL);
        VeilMuffle.register(source, sound);
    }

    static boolean available() {
        return available;
    }

    private static void setup() {
        available = false;
        long context = ALC10.alcGetCurrentContext();
        if (context == 0L || !ALC10.alcIsExtensionPresent(ALC10.alcGetContextsDevice(context), "ALC_EXT_EFX")) {
            TheabyssMod.LOGGER.warn("[AbyssReverb] OpenAL EFX not supported, no reverb in The Abyss");
            return;
        }
        try {
            slot = EXTEfx.alGenAuxiliaryEffectSlots();
            EXTEfx.alAuxiliaryEffectSloti(slot, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL11.AL_TRUE);
            effect = EXTEfx.alGenEffects();
            EXTEfx.alEffecti(effect, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
            appliedGain = -1.0F;
            appliedDecay = -1.0F;
            available = true;
            updateParameters();
        } catch (RuntimeException | LinkageError e) {
            TheabyssMod.LOGGER.warn("[AbyssReverb] could not create the reverb", e);
        }
    }

    private static void updateParameters() {
        float gain;
        float decay;
        try {
            gain = ConfigConfiguration.REVERB_STRENGTH.get() / 100.0F;
            decay = ConfigConfiguration.REVERB_LENGTH.get();
        } catch (IllegalStateException notLoadedYet) {
            gain = 1.0F;
            decay = 6.0F;
        }
        if (decay != appliedDecay) {
            EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DECAY_TIME, Math.clamp(decay, EXTEfx.AL_EAXREVERB_MIN_DECAY_TIME, EXTEfx.AL_EAXREVERB_MAX_DECAY_TIME));
            EXTEfx.alAuxiliaryEffectSloti(slot, EXTEfx.AL_EFFECTSLOT_EFFECT, effect);
            appliedDecay = decay;
        }
        if (gain != appliedGain) {
            EXTEfx.alAuxiliaryEffectSlotf(slot, EXTEfx.AL_EFFECTSLOT_GAIN, Math.clamp(gain, 0.0F, 1.0F));
            appliedGain = gain;
        }
    }

    static int source(Channel channel) {
        try {
            return ((ChannelAccessor)(Object)channel).theabyss$source();
        } catch (RuntimeException | LinkageError e) {
            available = false;
            TheabyssMod.LOGGER.warn("[AbyssReverb] cannot reach the sound source, reverb disabled", e);
            return 0;
        }
    }
}
