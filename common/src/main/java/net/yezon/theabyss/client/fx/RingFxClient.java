package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RingFxClient {
    public static final int SLOTS = 8;

    private record Effect(int kind, Vec3 at, float radius, long start, float seconds, int color, float param) {
    }

    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static Level level;

    private RingFxClient() {
    }

    public static void add(int kind, double x, double y, double z, float radius, int ticks, int color, float param) {
        if (EFFECTS.size() >= SLOTS) {
            EFFECTS.remove(0);
        }
        EFFECTS.add(new Effect(kind, new Vec3(x, y, z), radius, FxClock.nanos(), Math.max(1, ticks) / 20.0F, color & 0xFFFFFF, param));
    }

    private static final int RADIUS = 7;
    private static final int RADIUS_COLOR = 0x8FE8FF;
    private static final int LEASH_COLOR = 0xC8A8FF;

    public static boolean active() {
        return !EFFECTS.isEmpty() || TrueInvisibilityClient.localRadius() > 0.0F || ShadowVeilClient.localRadius() > 0.0F
            || EchoVisionClient.body() != null && EchoVisionClient.edge() > 0.01F;
    }

    public static void fill(Vec3 camera, float[] out) {
        long now = FxClock.nanos();
        for (int i = 0; i < SLOTS; i++) {
            int o = i * 8;
            if (i >= EFFECTS.size()) {
                out[o + 5] = 0.0F;
                if (i == SLOTS - 1) {
                    radiusSlot(camera, out, o);
                }
                continue;
            }
            Effect e = EFFECTS.get(i);
            if (i == SLOTS - 1 && radiusSlot(camera, out, o)) {
                continue;
            }
            out[o] = (float)(e.at.x - camera.x);
            out[o + 1] = (float)(e.at.y - camera.y);
            out[o + 2] = (float)(e.at.z - camera.z);
            out[o + 3] = e.radius;
            out[o + 4] = Math.min(1.0F, (now - e.start) / 1.0E9F / e.seconds);
            out[o + 5] = e.kind;
            out[o + 6] = e.color;
            out[o + 7] = e.param;
        }
    }

    private static boolean radiusSlot(Vec3 camera, float[] out, int o) {
        float radius = TrueInvisibilityClient.localRadius();
        float strength = 0.8F;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (radius <= 0.0F) {
            radius = ShadowVeilClient.localRadius();
            strength = ShadowVeilClient.circle();
        }
        if (radius <= 0.0F) {
            return leashSlot(camera, out, o);
        }
        Vec3 feet = mc.player.getPosition(mc.getTimer().getGameTimeDeltaPartialTick(true));
        out[o] = (float)(feet.x - camera.x);
        out[o + 1] = (float)(feet.y - camera.y);
        out[o + 2] = (float)(feet.z - camera.z);
        out[o + 3] = radius;
        out[o + 4] = 0.0F;
        out[o + 5] = RADIUS;
        out[o + 6] = RADIUS_COLOR;
        out[o + 7] = strength;
        return true;
    }

    private static boolean leashSlot(Vec3 camera, float[] out, int o) {
        Vec3 body = EchoVisionClient.body();
        float edge = EchoVisionClient.edge();
        if (body == null || edge <= 0.01F) {
            return false;
        }
        out[o] = (float)(body.x - camera.x);
        out[o + 1] = (float)(body.y - camera.y);
        out[o + 2] = (float)(body.z - camera.z);
        out[o + 3] = EchoVisionClient.range();
        out[o + 4] = 0.0F;
        out[o + 5] = RADIUS;
        out[o + 6] = LEASH_COLOR;
        out[o + 7] = 0.3F + 0.7F * edge;
        return true;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            level = mc.level;
            EFFECTS.clear();
        }
        long now = FxClock.nanos();
        EFFECTS.removeIf(e -> (now - e.start) / 1.0E9F > e.seconds);
    }
}
