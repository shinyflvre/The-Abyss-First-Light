package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.MovementInputUpdateEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.mixin.client.CameraPositionInvoker;
import net.yezon.theabyss.network.ClockRewindMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ClockRewindClient {
    private static final float REWIND = 0.75F;
    private static final float COLLAPSE_TIMEOUT = 2.5F;
    private static final float TUNNEL_IN = 0.15F;
    private static final float TUNNEL_OUT = 0.3F;
    private static final int SMOOTH_PASSES = 6;
    private static final float FOLLOW_RATE = 7.0F;
    private static final float SETTLE_RATE = 40.0F;
    private static final float ROLL = 3.0F;
    private static final float TICK_START = 0.11F;
    private static final float TICK_END = 0.045F;
    private static final double JUMP = 3.0;

    private enum Phase {
        NONE,
        RECENT,
        COLLAPSE,
        START
    }

    private static Phase phase = Phase.NONE;
    private static long phaseNanos;
    private static float nextTick;
    private static long startNanos;
    private static long endNanos;
    private static List<ClockRewindMessage.Sample> recent = List.of();
    private static List<ClockRewindMessage.Sample> start = List.of();
    private static ResourceKey<Level> dimension;
    private static Vec3 lastPosition = Vec3.ZERO;
    private static Vec3 hold = Vec3.ZERO;
    private static float holdYaw;
    private static float holdPitch;
    private static boolean worldFovPending;
    private static boolean cameraReady;
    private static Vec3 cameraEye = Vec3.ZERO;
    private static float cameraYaw;
    private static float cameraPitch;
    private static long cameraNanos;

    private ClockRewindClient() {
    }

    public static void start(List<ClockRewindMessage.Sample> recentPath, List<ClockRewindMessage.Sample> startPath) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        recent = smooth(reversed(recentPath));
        start = smooth(reversed(startPath));
        cameraReady = false;
        dimension = mc.level.dimension();
        lastPosition = mc.player.position();
        hold = mc.player.getEyePosition();
        holdYaw = mc.player.getYRot();
        holdPitch = mc.player.getXRot();
        startNanos = FxClock.nanos();
        endNanos = 0L;
        enter(recent.size() > 1 ? Phase.RECENT : Phase.COLLAPSE);
    }

    public static boolean active() {
        return phase != Phase.NONE;
    }

    public static boolean tunnel() {
        return phase != Phase.NONE || endNanos != 0L && (FxClock.nanos() - endNanos) / 1.0E9F < TUNNEL_OUT;
    }

    public static float tunnelTime() {
        return (FxClock.nanos() - startNanos) / 1.0E9F / REWIND;
    }

    public static float tunnelStrength() {
        float in = Mth.clamp((FxClock.nanos() - startNanos) / 1.0E9F / TUNNEL_IN, 0.0F, 1.0F);
        float out = endNanos == 0L ? 1.0F : 1.0F - Mth.clamp((FxClock.nanos() - endNanos) / 1.0E9F / TUNNEL_OUT, 0.0F, 1.0F);
        return in * out;
    }

    private static List<ClockRewindMessage.Sample> reversed(List<ClockRewindMessage.Sample> path) {
        List<ClockRewindMessage.Sample> copy = new ArrayList<>(path);
        Collections.reverse(copy);
        return copy;
    }

    private static List<ClockRewindMessage.Sample> smooth(List<ClockRewindMessage.Sample> path) {
        int n = path.size();
        if (n < 3) {
            return path;
        }
        double[][] v = new double[n][5];
        for (int i = 0; i < n; i++) {
            ClockRewindMessage.Sample s = path.get(i);
            float yaw = s.yaw();
            if (i > 0) {
                yaw = (float)v[i - 1][3] + Mth.wrapDegrees(yaw - (float)v[i - 1][3]);
            }
            v[i] = new double[]{s.eye().x, s.eye().y, s.eye().z, yaw, s.pitch()};
        }
        for (int pass = 0; pass < SMOOTH_PASSES; pass++) {
            double[][] next = new double[n][];
            next[0] = v[0];
            next[n - 1] = v[n - 1];
            for (int i = 1; i < n - 1; i++) {
                next[i] = new double[5];
                for (int c = 0; c < 5; c++) {
                    next[i][c] = (v[i - 1][c] + 2.0 * v[i][c] + v[i + 1][c]) * 0.25;
                }
            }
            v = next;
        }
        List<ClockRewindMessage.Sample> out = new ArrayList<>(n);
        for (double[] p : v) {
            out.add(new ClockRewindMessage.Sample(new Vec3(p[0], p[1], p[2]), (float)p[3], (float)p[4]));
        }
        return out;
    }

    private static void enter(Phase next) {
        if (next == Phase.START) {
            cameraReady = false;
        }
        phase = next;
        phaseNanos = FxClock.nanos();
        nextTick = 0.0F;
    }

    private static float elapsed() {
        return (FxClock.nanos() - phaseNanos) / 1.0E9F;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (phase == Phase.NONE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        float t = elapsed();
        boolean jumped = mc.level.dimension() != dimension || mc.player.position().distanceTo(lastPosition) > JUMP;
        lastPosition = mc.player.position();
        switch (phase) {
            case RECENT -> {
                ticking(mc, t);
                if (t >= REWIND) {
                    ClockRewindMessage.Sample last = recent.get(recent.size() - 1);
                    hold = last.eye();
                    holdYaw = last.yaw();
                    holdPitch = last.pitch();
                    enter(Phase.COLLAPSE);
                }
            }
            case COLLAPSE -> {
                if (jumped || t >= COLLAPSE_TIMEOUT) {
                    dimension = mc.level.dimension();
                    if (start.size() > 1) {
                        enter(Phase.START);
                    } else {
                        finish(mc);
                    }
                }
            }
            case START -> {
                ticking(mc, t);
                if (t >= REWIND) {
                    finish(mc);
                }
            }
            default -> {
            }
        }
    }

    private static void finish(Minecraft mc) {
        phase = Phase.NONE;
        endNanos = FxClock.nanos();
    }

    private static void ticking(Minecraft mc, float t) {
        if (t >= nextTick) {
            float k = Mth.clamp(t / REWIND, 0.0F, 1.0F);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.2F + 0.8F * k, 0.35F));
            nextTick = t + Mth.lerp(k, TICK_START, TICK_END);
        }
    }

    private static float progress() {
        float k = Mth.clamp(elapsed() / REWIND, 0.0F, 1.0F);
        return k * k * (3.0F - 2.0F * k);
    }

    private static ClockRewindMessage.Sample at(List<ClockRewindMessage.Sample> path, float u) {
        int n = path.size();
        float f = u * (n - 1);
        int i = Mth.clamp((int)f, 0, n - 2);
        float k = f - i;
        ClockRewindMessage.Sample p0 = path.get(Math.max(0, i - 1));
        ClockRewindMessage.Sample p1 = path.get(i);
        ClockRewindMessage.Sample p2 = path.get(i + 1);
        ClockRewindMessage.Sample p3 = path.get(Math.min(n - 1, i + 2));
        Vec3 eye = new Vec3(spline(p0.eye().x, p1.eye().x, p2.eye().x, p3.eye().x, k), spline(p0.eye().y, p1.eye().y, p2.eye().y, p3.eye().y, k),
            spline(p0.eye().z, p1.eye().z, p2.eye().z, p3.eye().z, k));
        float yaw = (float)spline(p0.yaw(), p1.yaw(), p2.yaw(), p3.yaw(), k);
        float pitch = (float)spline(p0.pitch(), p1.pitch(), p2.pitch(), p3.pitch(), k);
        return new ClockRewindMessage.Sample(eye, yaw, pitch);
    }

    private static double spline(double p0, double p1, double p2, double p3, float t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * (2.0 * p1 + (p2 - p0) * t + (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 + (3.0 * p1 - p0 - 3.0 * p2 + p3) * t3);
    }

    private static void follow(ClockRewindMessage.Sample target) {
        long now = FxClock.nanos();
        float dt = cameraReady ? Mth.clamp((now - cameraNanos) / 1.0E9F, 0.0F, 0.1F) : 0.0F;
        cameraNanos = now;
        if (!cameraReady) {
            cameraReady = true;
            cameraEye = target.eye();
            cameraYaw = target.yaw();
            cameraPitch = target.pitch();
            return;
        }
        float settle = phase == Phase.START ? Mth.clamp((progress() - 0.75F) / 0.25F, 0.0F, 1.0F) : 0.0F;
        float rate = Mth.lerp(settle * settle, FOLLOW_RATE, SETTLE_RATE);
        float k = 1.0F - (float)Math.exp(-rate * dt);
        cameraEye = cameraEye.lerp(target.eye(), k);
        cameraYaw += Mth.wrapDegrees(target.yaw() - cameraYaw) * k;
        cameraPitch += (target.pitch() - cameraPitch) * k;
    }

    private static ClockRewindMessage.Sample view(Minecraft mc, float partialTick) {
        return switch (phase) {
            case RECENT -> at(recent, progress());
            case COLLAPSE -> new ClockRewindMessage.Sample(hold, holdYaw, holdPitch);
            case START -> {
                float u = progress();
                ClockRewindMessage.Sample onPath = at(start, u);
                float blend = Mth.clamp((u - 0.8F) / 0.2F, 0.0F, 1.0F);
                Vec3 eye = mc.player.getEyePosition(partialTick);
                yield new ClockRewindMessage.Sample(onPath.eye().lerp(eye, blend), Mth.rotLerp(blend, onPath.yaw(), mc.player.getYRot()),
                    Mth.lerp(blend, onPath.pitch(), mc.player.getXRot()));
            }
            default -> null;
        };
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!controlsCamera(mc)) {
            return;
        }
        worldFovPending = true;
        ClockRewindMessage.Sample view = view(mc, (float)event.getPartialTick());
        if (view == null) {
            return;
        }
        follow(view);
        event.setYaw(cameraYaw);
        event.setPitch(cameraPitch);
        float spin = phase == Phase.COLLAPSE ? 1.0F : Mth.sin(progress() * Mth.PI);
        event.setRoll(event.getRoll() + ROLL * Mth.sin(tunnelTime() * Mth.PI) * spin);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (!controlsCamera(mc)) {
            return;
        }
        if (worldFovPending) {
            worldFovPending = false;
            if (cameraReady) {
                move(event.getCamera(), cameraEye);
            }
        }
        float squeeze = switch (phase) {
            case RECENT, START -> 0.12F * Mth.sin(progress() * Mth.PI);
            case COLLAPSE -> 0.14F;
            default -> 0.0F;
        };
        event.setFOV(event.getFOV() * (1.0F - squeeze));
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (phase == Phase.NONE) {
            return;
        }
        Input input = event.getInput();
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
    }

    private static boolean controlsCamera(Minecraft mc) {
        return phase != Phase.NONE && mc.player != null && mc.getCameraEntity() == mc.player;
    }

    private static void move(Camera camera, Vec3 position) {
        ((CameraPositionInvoker)camera).theabyss$setPosition(position);
    }
}
