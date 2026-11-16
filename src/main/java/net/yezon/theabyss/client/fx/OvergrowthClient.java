package net.yezon.theabyss.client.fx;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.infection.Overgrowth;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class OvergrowthClient {
    private static final float PULSE_SECONDS = 0.35F;
    private static final float JOLT_SECONDS = 0.3F;
    private static final float KNEEL = 0.75F;
    private static final float SINK = 2.6F;
    private static final int NIGHTBLADE_FROM = 74;
    private static final int NIGHTBLADE_UNTIL = 94;
    private static final int NIGHTBLADE_ID = -0x4E1B1ADE;
    private static final RandomSource RANDOM = RandomSource.create();

    private record Other(float stage, int collapse, long startNanos) {
    }

    private static final Map<Integer, Other> OTHERS = new HashMap<>();
    private static float stage = -1.0F;
    private static float shownStage;
    private static float pulse;
    private static float jolt;
    private static float joltSign = 1.0F;
    private static int collapseTicks = -1;
    private static long collapseNanos;
    private static long lastNanos;
    private static boolean worldFovPending;
    private static Entity nightblade;
    private static boolean pushed;

    private OvergrowthClient() {
    }

    public static void update(int entityId, float newStage, int flags, int collapse) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        boolean self = mc.player != null && mc.player.getId() == entityId;
        if (!self) {
            if (newStage < 0.0F) {
                OTHERS.remove(entityId);
            } else {
                Other old = OTHERS.get(entityId);
                long start = collapse >= 0 && (old == null || old.collapse < 0) ? FxClock.nanos() - collapse * 50_000_000L
                    : old != null ? old.startNanos : 0L;
                OTHERS.put(entityId, new Other(newStage, collapse, start));
            }
            return;
        }
        if (newStage < 0.0F) {
            stage = -1.0F;
            collapseTicks = -1;
            removeNightblade();
            return;
        }
        stage = newStage;
        if (collapse >= 0) {
            if (collapseTicks < 0) {
                collapseNanos = FxClock.nanos() - collapse * 50_000_000L;
            }
            collapseTicks = collapse;
        }
        if ((flags & 1) != 0) {
            pulse = 1.0F;
            float volume = 0.6F + 0.5F * stage + (collapseTicks >= 0 ? 0.3F : 0.0F);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_HEARTBEAT, 0.9F - 0.1F * stage, Math.min(1.0F, volume)));
        }
        if ((flags & 2) != 0) {
            jolt = 1.0F;
            joltSign = RANDOM.nextBoolean() ? 1.0F : -1.0F;
            if (mc.player != null) {
                mc.player.setSprinting(false);
            }
        }
    }

    public static boolean active() {
        return stage >= 0.0F;
    }

    public static float stage() {
        return shownStage;
    }

    public static float pulse() {
        return pulse;
    }

    public static float collapse() {
        if (collapseTicks < 0) {
            return 0.0F;
        }
        float ticks = (FxClock.nanos() - collapseNanos) / 5.0E7F;
        return Mth.clamp(ticks / Overgrowth.COLLAPSE_TICKS, 0.0F, 1.0F);
    }

    public static boolean overgrown(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getId() == entityId ? active() : OTHERS.containsKey(entityId);
    }

    private static float sinkOf(float c) {
        float kneel = Mth.clamp(c / 0.3F, 0.0F, 1.0F);
        float sink = Mth.clamp((c - 0.3F) / 0.7F, 0.0F, 1.0F);
        return KNEEL * kneel * kneel * (3.0F - 2.0F * kneel) + (SINK - KNEEL) * sink * sink;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        long now = FxClock.nanos();
        float dt = lastNanos == 0L ? 0.0F : Mth.clamp((now - lastNanos) / 1.0E9F, 0.0F, 0.1F);
        lastNanos = now;
        float target = active() ? Math.max(stage, 0.05F) : 0.0F;
        shownStage += (target - shownStage) * Math.min(1.0F, dt * 2.0F);
        if (!active() && shownStage < 0.002F) {
            shownStage = 0.0F;
        }
        pulse = Math.max(0.0F, pulse - dt / PULSE_SECONDS);
        jolt = Math.max(0.0F, jolt - dt / JOLT_SECONDS);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.isPaused()) {
            return;
        }
        if (!active()) {
            removeNightblade();
            return;
        }
        if (collapseTicks >= 0) {
            int ticks = Math.round((FxClock.nanos() - collapseNanos) / 5.0E7F);
            if (ticks >= NIGHTBLADE_FROM && ticks < NIGHTBLADE_UNTIL && nightblade == null) {
                spawnNightblade(level, player);
            } else if (ticks >= NIGHTBLADE_UNTIL) {
                removeNightblade();
            }
        }
    }

    private static void spawnNightblade(ClientLevel level, LocalPlayer player) {
        Entity e = TheabyssModEntities.RIFT_NIGHTBLADE.get().create(level, EntitySpawnReason.LOAD);
        if (e == null) {
            return;
        }
        Vec3 look = player.getViewVector(1.0F);
        Vec3 flat = new Vec3(look.x, 0.0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
        Vec3 at = player.position().add(flat.scale(2.2));
        float yaw = (float)(Mth.atan2(-flat.z, -flat.x) * Mth.RAD_TO_DEG) - 90.0F;
        e.setId(NIGHTBLADE_ID);
        e.snapTo(at.x, player.getY() - 0.8, at.z, yaw, 0.0F);
        e.setYHeadRot(yaw);
        e.setYBodyRot(yaw);
        level.addEntity(e);
        nightblade = e;
    }

    private static void removeNightblade() {
        if (nightblade == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            level.removeEntity(NIGHTBLADE_ID, Entity.RemovalReason.DISCARDED);
        }
        nightblade = null;
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || !active() || sound instanceof TickableSoundInstance || sound.isRelative() || sound.getSource() == SoundSource.MUSIC
            || sound.getSource() == SoundSource.MASTER) {
            return;
        }
        String path = sound.getIdentifier().getPath();
        if (path.contains("heartbeat") || path.contains("night_notice")) {
            return;
        }
        float c = collapse();
        float volume = Mth.clamp(1.0F - 0.55F * stage - 0.35F * c, 0.08F, 1.0F);
        float pitch = 1.0F - 0.08F * stage - 0.1F * c;
        event.setSound(new MuffledSound(sound, volume, pitch));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getCameraEntity() != mc.player || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        worldFovPending = true;
        if (jolt > 0.0F) {
            float j = jolt * jolt;
            event.setPitch(event.getPitch() + 2.2F * j);
            event.setRoll(event.getRoll() + 1.6F * j * joltSign);
        }
        float c = collapse();
        if (c > 0.0F) {
            float down = Mth.clamp(c / 0.6F, 0.0F, 1.0F);
            event.setPitch(Mth.lerp(down * down * 0.7F, event.getPitch(), 65.0F));
            event.setRoll(event.getRoll() + 4.0F * c * Mth.sin(c * 9.0F));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (!worldFovPending) {
            return;
        }
        worldFovPending = false;
        float c = collapse();
        if (c > 0.0F && mc.player != null) {
            Camera camera = event.getCamera();
            TeleportDashClient.moveCamera(camera, camera.position().add(0.0, -sinkOf(c), 0.0));
        }
        if (pulse > 0.0F) {
            event.setFOV(event.getFOV() * (1.0F - 0.025F * pulse * Math.max(0.3F, stage)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
        pushed = false;
        if (event.isCanceled() || OTHERS.isEmpty()) {
            return;
        }
        Other other = OTHERS.get(event.getRenderState().id);
        if (other == null || other.collapse < 0) {
            return;
        }
        float ticks = (FxClock.nanos() - other.startNanos) / 5.0E7F;
        float c = Mth.clamp(ticks / Overgrowth.COLLAPSE_TICKS, 0.0F, 1.0F);
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(0.0F, -sinkOf(c), 0.0F);
        pushed = true;
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
        if (pushed) {
            event.getPoseStack().popPose();
            pushed = false;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        OTHERS.clear();
        stage = -1.0F;
        shownStage = 0.0F;
        collapseTicks = -1;
        nightblade = null;
    }
}
