package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeStormClient {
    public static final int SLOTS = 12;
    private static final float FLASH_SECONDS = 0.4F;
    private static final float DARKNESS = 1.0F;
    private static final float QUIET_DARKNESS = 0.75F;
    private static boolean lastQuiet;

    private static final float RED_FLAG = 10.0F;
    private static final float QUIET_FLAG = 20.0F;
    private static final float LANTERN_FLAG = 40.0F;

    private record Ring(double x, double y, double z, long spawnNanos, float delaySeconds, boolean red, boolean quiet) {
    }

    private static final List<Ring> RINGS = new ArrayList<>();
    private static int bossId = -1;
    private static long lastRingEnd;
    private static float darkness;
    private static long lastNanos;
    private static Level level;
    private static float phaseDarkness;
    private static final float PHASE_2_DARKNESS = 0.5F;
    private static final float PHASE_3_DARKNESS = 0.66F;
    private static final double PHASE_RANGE = 48.0;

    private NightbladeStormClient() {
    }

    public static void addRing(int boss, double x, double y, double z, int delayTicks, boolean red) {
        addRing(boss, x, y, z, delayTicks, red, false);
    }

    public static void addRing(int boss, double x, double y, double z, int delayTicks, boolean red, boolean quiet) {
        bossId = boss;
        RINGS.add(new Ring(x, y, z, FxClock.nanos(), delayTicks / 20.0F, red, quiet));
    }

    public static boolean active() {
        return darkness > 0.003F || !RINGS.isEmpty() || TelekinesisClient.active();
    }

    public static float darkness() {
        return darkness;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long now = FxClock.nanos();
        float dt = lastNanos == 0L ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        if (mc.level != level) {
            level = mc.level;
            RINGS.clear();
            darkness = 0.0F;
        }
        RINGS.removeIf(ring -> {
            boolean over = (now - ring.spawnNanos) / 1.0E9F > ring.delaySeconds + FLASH_SECONDS;
            if (over) {
                lastRingEnd = now;
                lastQuiet = ring.quiet;
            }
            return over;
        });
        boolean storm = !RINGS.isEmpty() || (now - lastRingEnd) / 1.0E9F < 1.0F;
        boolean quietOnly = RINGS.isEmpty() ? lastQuiet : RINGS.stream().allMatch(Ring::quiet);
        float target = Math.max(storm ? (quietOnly ? QUIET_DARKNESS : DARKNESS) : 0.0F, phaseDarkness);
        float chains = TelekinesisClient.darkness();
        if (chains >= target && chains > 0.0F) {
            darkness = chains;
            return;
        }
        darkness = Mth.approach(darkness, target, dt / (target > darkness ? (storm ? 0.5F : 2.5F) : (storm ? 1.2F : 3.0F)));
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        phaseDarkness = 0.0F;
        if (mc.player == null || mc.level == null) {
            return;
        }
        net.yezon.theabyss.entity.NightbladeBossEntity nearest = null;
        for (var boss : mc.level.getEntitiesOfClass(net.yezon.theabyss.entity.NightbladeBossEntity.class, mc.player.getBoundingBox().inflate(PHASE_RANGE))) {
            if (boss.isAlive() && !boss.isMirrorSynced() && (nearest == null || boss.distanceToSqr(mc.player) < nearest.distanceToSqr(mc.player))) {
                nearest = boss;
            }
        }
        if (nearest != null) {
            phaseDarkness = nearest.getPhase() >= 3 ? PHASE_3_DARKNESS : nearest.getPhase() == 2 ? PHASE_2_DARKNESS : 0.0F;
            if (RINGS.isEmpty()) {
                bossId = nearest.getId();
            }
        }
    }

    public static Vec3 bossRelativeTo(Vec3 camera) {
        Minecraft mc = Minecraft.getInstance();
        Entity boss = mc.level != null && bossId >= 0 ? mc.level.getEntity(bossId) : null;
        return boss != null ? boss.position().add(0.0, boss.getBbHeight() * 0.5, 0.0).subtract(camera) : new Vec3(0.0, -10000.0, 0.0);
    }

    public static void fillSlots(Vec3 camera, float[] out) {
        long now = FxClock.nanos();
        int slot = 0;
        for (Ring ring : RINGS) {
            if (slot >= SLOTS) {
                break;
            }
            float age = (now - ring.spawnNanos) / 1.0E9F;
            float progress = age < ring.delaySeconds ? age / ring.delaySeconds : 1.0F + (age - ring.delaySeconds) / FLASH_SECONDS;
            out[slot * 4] = (float)(ring.x - camera.x);
            out[slot * 4 + 1] = (float)(ring.y - camera.y);
            out[slot * 4 + 2] = (float)(ring.z - camera.z);
            out[slot * 4 + 3] = Math.min(progress, 2.0F) + (ring.red ? RED_FLAG : 0.0F) + (ring.quiet ? QUIET_FLAG : 0.0F);
            slot++;
        }
        List<float[]> lanterns = new ArrayList<>();
        TelekinesisClient.lanterns(lanterns, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        for (float[] l : lanterns) {
            if (slot >= SLOTS) {
                break;
            }
            out[slot * 4] = (float)(l[0] - camera.x);
            out[slot * 4 + 1] = (float)(l[1] - camera.y);
            out[slot * 4 + 2] = (float)(l[2] - camera.z);
            out[slot * 4 + 3] = LANTERN_FLAG + Mth.clamp(l[3], 0.0F, 1.0F);
            slot++;
        }
        for (; slot < SLOTS; slot++) {
            out[slot * 4 + 3] = -1.0F;
        }
    }
}
