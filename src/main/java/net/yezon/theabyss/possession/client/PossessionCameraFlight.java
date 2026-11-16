package net.yezon.theabyss.possession.client;

import java.lang.reflect.Field;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.possession.PossessionData;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PossessionCameraFlight {
    private static final float SECONDS = 1.0F;
    private static final float ROLL_DEGREES = 16.0F;
    private static final float FOV_PULL = 0.16F;
    private static final float HEAD_RADIUS = 0.45F;

    private static Vec3 lastFreeEye;
    private static float lastYaw;
    private static float lastPitch;

    private static Marker eye;
    private static int targetId = -1;
    private static int flownTo = -1;
    private static long startNanos;
    private static Vec3 from;
    private static float fromYaw;
    private static float fromPitch;

    private static Field eyeHeightField;
    private static Field eyeHeightOldField;
    private static boolean reflectionFailed;

    private PossessionCameraFlight() {
    }

    public static boolean flying() {
        return eye != null;
    }

    static boolean shouldStart(Entity mob) {
        return mob.getId() != flownTo && lastFreeEye != null;
    }

    static void start(Minecraft mc, Entity mob) {
        eye = new Marker(EntityTypes.MARKER, mc.level);
        targetId = mob.getId();
        flownTo = mob.getId();
        from = lastFreeEye;
        fromYaw = lastYaw;
        fromPitch = lastPitch;
        startNanos = Util.getNanos();
        place(mc, mob, 0.0F, 1.0F);
        eye.setOldPosAndRot();
        mc.setCameraEntity(eye);
    }

    static void stop(Minecraft mc, Entity next) {
        eye = null;
        targetId = -1;
        mc.setCameraEntity(next);
    }

    private static float progress() {
        return Mth.clamp((Util.getNanos() - startNanos) / 1.0E9F / SECONDS, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !PossessionData.of(mc.player).active()) {
            lastFreeEye = mc.player.getEyePosition();
            lastYaw = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
            flownTo = -1;
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (eye == null || mc.level == null || mc.player == null) {
            return;
        }
        Entity mob = mc.level.getEntity(targetId);
        if (mob == null || !mob.isAlive() || !PossessionData.of(mc.player).active()) {
            stop(mc, mc.player);
            return;
        }
        float t = progress();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        if (t >= 1.0F || t > 0.5F && cameraPosition(mob, t, partialTick).distanceTo(mob.getEyePosition(partialTick)) < HEAD_RADIUS) {
            stop(mc, mob);
            return;
        }
        place(mc, mob, t, partialTick);
    }

    private static Vec3 cameraPosition(Entity mob, float t, float partialTick) {
        Vec3 to = mob.getEyePosition(partialTick);
        float s = t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
        return from.lerp(to, s).add(0.0, Math.sin(Math.PI * t) * 0.6, 0.0);
    }

    private static float approach(Minecraft mc) {
        Entity mob = mc.level != null ? mc.level.getEntity(targetId) : null;
        if (mob == null || eye == null) {
            return 0.0F;
        }
        double distance = eye.position().add(0.0, cameraEyeHeight(mc.gameRenderer.mainCamera(), 1.0F), 0.0).distanceTo(mob.getEyePosition());
        return smoothstep(HEAD_RADIUS, HEAD_RADIUS + 1.5F, (float)distance);
    }

    private static void place(Minecraft mc, Entity mob, float t, float partialTick) {
        Vec3 to = mob.getEyePosition(partialTick);
        Vec3 pos = cameraPosition(mob, t, partialTick);

        float playerYaw = mc.player.getViewYRot(partialTick);
        float playerPitch = mc.player.getViewXRot(partialTick);
        Vec3 toHead = to.subtract(pos);
        float lookYaw = playerYaw;
        float lookPitch = playerPitch;
        if (toHead.lengthSqr() > 0.04) {
            lookYaw = (float)(Mth.atan2(toHead.z, toHead.x) * Mth.RAD_TO_DEG) - 90.0F;
            lookPitch = (float)(-Mth.atan2(toHead.y, toHead.horizontalDistance()) * Mth.RAD_TO_DEG);
        }
        float turnIn = smoothstep(0.0F, 0.3F, t);
        float settle = smoothstep(0.55F, 1.0F, t);
        float yaw = Mth.rotLerp(settle, Mth.rotLerp(turnIn, fromYaw, lookYaw), playerYaw);
        float pitch = Mth.lerp(settle, Mth.lerp(turnIn, fromPitch, lookPitch), playerPitch);

        double cameraEye = cameraEyeHeight(mc.gameRenderer.mainCamera(), partialTick);
        eye.setPos(pos.x, pos.y - cameraEye, pos.z);
        eye.xo = eye.getX();
        eye.yo = eye.getY();
        eye.zo = eye.getZ();
        eye.setYRot(yaw);
        eye.yRotO = yaw;
        eye.setXRot(pitch);
        eye.xRotO = pitch;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (eye != null) {
            float t = progress();
            event.setRoll(event.getRoll() - ROLL_DEGREES * Mth.sin(t * Mth.TWO_PI) * (1.0F - 0.3F * t) * approach(Minecraft.getInstance()));
        }
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (eye != null) {
            event.setFOV(event.getFOV() * (1.0F + FOV_PULL * Mth.sin(progress() * Mth.PI) * approach(Minecraft.getInstance())));
        }
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float k = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return k * k * (3.0F - 2.0F * k);
    }

    private static double cameraEyeHeight(Camera camera, float partialTick) {
        if (reflectionFailed) {
            return 0.0;
        }
        try {
            if (eyeHeightField == null) {
                eyeHeightField = Camera.class.getDeclaredField("eyeHeight");
                eyeHeightOldField = Camera.class.getDeclaredField("eyeHeightOld");
                eyeHeightField.setAccessible(true);
                eyeHeightOldField.setAccessible(true);
            }
            return Mth.lerp(partialTick, eyeHeightOldField.getFloat(camera), eyeHeightField.getFloat(camera));
        } catch (ReflectiveOperationException | RuntimeException e) {
            reflectionFailed = true;
            TheabyssMod.LOGGER.warn("Possession camera flight: camera eye height not readable, flight may be a bit off", e);
            return 0.0;
        }
    }
}
