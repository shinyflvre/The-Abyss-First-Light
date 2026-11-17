package net.yezon.theabyss.weather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.sound.PlaySoundEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.weather.AbyssStorm;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssThunderClient {
    private static final ResourceLocation THUNDER = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_thunder");
    private static final ResourceLocation IMPACT = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_thunder_impact");
    private static final float VOLUME = 0.5F;
    private static final float MIN_PITCH = 0.8F;
    private static final float MAX_PITCH = 1.2F;
    private static final RandomSource RANDOM = RandomSource.create();

    private AbyssThunderClient() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        Minecraft mc = Minecraft.getInstance();
        if (sound == null || mc.level == null || !mc.level.dimension().equals(AbyssStorm.THE_ABYSS)) {
            return;
        }
        ResourceLocation id = sound.getLocation();
        if (id.equals(SoundEvents.LIGHTNING_BOLT_THUNDER.getLocation())) {
            float pitch = MIN_PITCH + RANDOM.nextFloat() * (MAX_PITCH - MIN_PITCH);
            event.setSound(new SimpleSoundInstance(THUNDER, SoundSource.WEATHER, VOLUME, pitch, RANDOM, false, 0,
                SoundInstance.Attenuation.NONE, sound.getX(), sound.getY(), sound.getZ(), false));
        } else if (id.equals(SoundEvents.LIGHTNING_BOLT_IMPACT.getLocation())) {
            float pitch = 0.5F + RANDOM.nextFloat() * 0.2F;
            event.setSound(new SimpleSoundInstance(IMPACT, SoundSource.WEATHER, VOLUME, pitch, RANDOM, false, 0,
                SoundInstance.Attenuation.LINEAR, sound.getX(), sound.getY(), sound.getZ(), false));
        }
    }
}
