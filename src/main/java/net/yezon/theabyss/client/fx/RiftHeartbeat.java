package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.rift.RiftOfSins;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RiftHeartbeat {
    private static final int INTERVAL = 30;
    private static final float VOLUME = 0.55F;
    private static final float PITCH = 0.85F;
    private static final RandomSource RANDOM = RandomSource.create();
    private static int nextBeat;

    private RiftHeartbeat() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !RiftOfSins.isRift(mc.level)) {
            nextBeat = 10;
            return;
        }
        if (mc.isPaused() || FearClient.intensity() > 0.0F || --nextBeat > 0) {
            return;
        }
        nextBeat = INTERVAL;
        mc.getSoundManager().play(new SimpleSoundInstance(SoundEvents.WARDEN_HEARTBEAT.location(), SoundSource.MASTER, VOLUME, PITCH, RANDOM, false, 0,
            SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true));
    }
}
