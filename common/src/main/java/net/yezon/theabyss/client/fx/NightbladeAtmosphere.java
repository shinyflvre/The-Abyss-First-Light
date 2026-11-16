package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeBossEntity;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeAtmosphere {
    private static final double RANGE = 56.0;
    private static final float RIPPLE_SECONDS = 0.9F;
    private static float presence;
    private static float heartbeat;
    private static int seenPhase;
    private static long rippleStart = -1L;
    private static long lastNanos;

    private NightbladeAtmosphere() {
    }

    public static float presence() {
        return presence;
    }

    public static float heartbeat() {
        return heartbeat;
    }

    public static float ripple() {
        if (rippleStart < 0L) {
            return 0.0F;
        }
        float t = (Util.getNanos() - rippleStart) / 1.0E9F / RIPPLE_SECONDS;
        if (t >= 1.0F) {
            rippleStart = -1L;
            return 0.0F;
        }
        return Math.max(t, 0.004F);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getNanos();
        float dt = lastNanos == 0L || mc.isPaused() ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        int phase = 0;
        int nightblades = 0;
        if (mc.level != null && mc.player != null) {
            for (NightbladeBossEntity boss : mc.level.getEntitiesOfClass(NightbladeBossEntity.class, mc.player.getBoundingBox().inflate(RANGE))) {
                if (!boss.isAlive()) {
                    continue;
                }
                nightblades++;
                if (!boss.isMirrorSynced()) {
                    phase = Math.max(phase, boss.getPhase());
                }
            }
        }
        float target = phase == 0 ? 0.0F : phase == 1 ? 0.35F : phase == 2 ? 0.55F : 0.75F;
        if (phase > 0 && nightblades >= 3) {
            target += 0.25F;
        }
        presence = Mth.approach(presence, target, dt / 1.5F);
        heartbeat = Mth.approach(heartbeat, phase == 3 ? 1.0F : 0.0F, dt / 2.0F);
        if (phase > seenPhase && seenPhase > 0) {
            rippleStart = now;
            CameraShake.add(3.0F, 0.8F);
        }
        seenPhase = phase;
    }
}
