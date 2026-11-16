package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent;
import net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent;
import net.yezon.theabyss.platform.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.ring.ComeHere;
import net.yezon.theabyss.ring.Telekinesis;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TelekinesisClient {
    private static final RenderType CHAIN = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/telekinetic_chain.png"));
    private static final ContextKey<List<Chain>> CHAINS = new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "telekinesis_chains"));
    private static final int OUTLINE = ARGB.opaque(0xA448FF);
    private static final int GLOW = 0x8A2EE0;
    private static final float DARKNESS = 0.82F;
    private static final float WIDTH = 0.24F;
    private static final float TILE = 0.56F;
    private static final float STEP = TILE / 2.0F;
    private static final float RETRACT = 6.0F;
    private static final int SPIRAL = 0xE0C0FF;
    private static final int HOOP = 0xB45CFF;
    private static final double SPIRAL_RADIUS = 0.17;
    private static final double SPIRAL_TURN = 1.2;
    private static final int SPIRAL_STEPS = 3;

    private static final class Cast {
        final int casterId;
        final List<Integer> targets;
        final long startNanos;
        final Map<Integer, Float> impactAge = new HashMap<>();
        final int slamTick;
        final boolean pull;
        final boolean parry;

        Cast(int casterId, List<Integer> targets, boolean pull, boolean parry) {
            this.casterId = casterId;
            this.targets = targets;
            this.startNanos = FxClock.nanos();
            this.pull = pull;
            this.parry = parry;
            this.slamTick = pull ? ComeHere.release() : Telekinesis.slam(targets.size());
        }

        float age() {
            return (FxClock.nanos() - this.startNanos) / 5.0E7F;
        }

        float endAge() {
            return this.pull ? this.slamTick + RETRACT + 4 : this.slamTick + Telekinesis.SLAM_MAX + RETRACT + 4;
        }
    }

    private record Chain(Vec3[] points, float length, float twist, float glow, boolean taper, float age) {
    }

    private static final List<Cast> CASTS = new ArrayList<>();
    private static Level level;

    private TelekinesisClient() {
    }

    public static void start(int casterId, List<Integer> targets) {
        CASTS.add(new Cast(casterId, targets, false, false));
    }

    public static void pull(int casterId, List<Integer> targets, boolean parry) {
        CASTS.add(new Cast(casterId, targets, true, parry));
        Minecraft mc = Minecraft.getInstance();
        if (parry && mc.player != null && mc.player.getId() == casterId && !targets.isEmpty()) {
            ParryCue.set(-200_000 - targets.get(0), ComeHere.arrival(), ComeHere.WINDOW, true);
        }
    }

    public static boolean pullParry() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        prune();
        for (Cast cast : CASTS) {
            if (cast.pull && cast.parry && cast.casterId == mc.player.getId() && cast.impactAge.isEmpty() && cast.age() < cast.slamTick + 2) {
                return true;
            }
        }
        return false;
    }

    public static void impact(int casterId, int targetId) {
        for (Cast cast : CASTS) {
            if (cast.casterId == casterId && cast.targets.contains(targetId)) {
                cast.impactAge.putIfAbsent(targetId, cast.age());
            }
        }
    }

    private static void prune() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            level = mc.level;
            CASTS.clear();
        }
        CASTS.removeIf(cast -> cast.age() > cast.endAge() + 30.0F);
    }

    public static boolean active() {
        prune();
        return !CASTS.isEmpty();
    }

    public static float darkness() {
        prune();
        float d = 0.0F;
        for (Cast cast : CASTS) {
            if (cast.pull) {
                continue;
            }
            float age = cast.age();
            float in = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
            float out = 1.0F - Mth.clamp((age - cast.endAge()) / 30.0F, 0.0F, 1.0F);
            d = Math.max(d, DARKNESS * in * out);
        }
        return d;
    }

    public static void lanterns(List<float[]> out, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (Cast cast : CASTS) {
            float age = cast.age();
            for (int i = 0; i < cast.targets.size(); i++) {
                float light = boundLight(cast, i, age);
                Entity e = mc.level.getEntity(cast.targets.get(i));
                if (light > 0.0F && e != null) {
                    Vec3 p = e.getPosition(partialTick).add(0.0, e.getBbHeight() * 0.5, 0.0);
                    out.add(new float[] {(float)p.x, (float)p.y, (float)p.z, light});
                }
            }
        }
    }

    private static float boundLight(Cast cast, int i, float age) {
        int latch = Telekinesis.latch(i);
        float in = Mth.clamp((age - (latch - 3)) / 4.0F, 0.0F, 1.0F);
        Float hit = cast.impactAge.get(cast.targets.get(i));
        float out = hit == null ? 1.0F : 1.0F - Mth.clamp((age - hit) / 12.0F, 0.0F, 1.0F);
        return in * out;
    }

    private static boolean outlined(int entityId) {
        for (Cast cast : CASTS) {
            int i = cast.targets.indexOf(entityId);
            if (i >= 0 && boundLight(cast, i, cast.age()) > 0.2F) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class<EntityRenderer<Entity, EntityRenderState>>)(Class<?>)EntityRenderer.class, (entity, state) -> {
            if (!CASTS.isEmpty() && outlined(entity.getId())) {
                state.outlineColor = OUTLINE;
            }
        });
    }

    @SubscribeEvent
    public static void onExtract(ExtractLevelRenderStateEvent event) {
        if (!active()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float pt = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().position();
        List<Chain> chains = new ArrayList<>();
        for (Cast cast : CASTS) {
            Entity caster = mc.level.getEntity(cast.casterId);
            if (caster == null) {
                continue;
            }
            boolean own = caster == mc.player && mc.options.getCameraType().isFirstPerson();
            Vec3 hand = hand(mc, caster, pt).subtract(camera);
            float age = cast.age();
            for (int i = 0; i < cast.targets.size(); i++) {
                Entity target = mc.level.getEntity(cast.targets.get(i));
                if (target != null) {
                    Chain chain = chain(cast, i, age, hand, target.getPosition(pt).add(0.0, target.getBbHeight() * 0.55, 0.0).subtract(camera), own);
                    if (chain != null) {
                        chains.add(chain);
                    }
                }
            }
        }
        net.yezon.theabyss.platform.client.renderstate.RenderData.set(event.getRenderState(), CHAINS, chains);
    }

    private static Vec3 hand(Minecraft mc, Entity caster, float pt) {
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        if (caster == mc.player && mc.options.getCameraType().isFirstPerson()) {
            Vec3 look = caster.getViewVector(pt);
            Vec3 right = look.cross(up);
            right = right.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
            Vec3 down = right.cross(look).normalize();
            return caster.getEyePosition(pt).add(look.scale(1.4)).add(right.scale(0.85)).subtract(down.scale(0.85));
        }
        float yaw = (caster instanceof LivingEntity living ? living.getPreciseBodyRotation(pt) : caster.getYRot(pt)) * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        return caster.getPosition(pt).add(0.0, caster.getBbHeight() * 0.62, 0.0).add(right.scale(0.38)).add(forward.scale(0.2));
    }

    private static Chain chain(Cast cast, int i, float age, Vec3 hand, Vec3 target, boolean own) {
        int launch = Telekinesis.launch(i);
        int latch = Telekinesis.latch(i);
        if (age < launch) {
            return null;
        }
        Float hit = cast.impactAge.get(cast.targets.get(i));
        if (hit == null && cast.pull && age >= cast.slamTick) {
            hit = (float)cast.slamTick;
        }
        Vec3 tip;
        float amplitude;
        float glow;
        if (hit != null) {
            float g = (age - hit) / RETRACT;
            if (g >= 1.0F) {
                return null;
            }
            float back = g * g;
            tip = target.add(hand.subtract(target).scale(back));
            amplitude = 0.55F * (1.0F - g);
            glow = 1.0F - g;
        } else if (age < latch) {
            float f = (age - launch) / Telekinesis.FLIGHT;
            float ease = 1.0F - (1.0F - f) * (1.0F - f);
            tip = hand.add(target.subtract(hand).scale(ease));
            amplitude = 0.9F * (1.0F - f);
            glow = 0.45F;
        } else if (cast.pull) {
            tip = target;
            amplitude = 0.06F;
            glow = 0.9F + 0.2F * Mth.sin(age * 2.0F);
        } else if (age < cast.slamTick) {
            tip = target;
            float hang = Mth.clamp((age - (cast.slamTick - Telekinesis.HANG)) / Telekinesis.HANG, 0.0F, 1.0F);
            amplitude = 0.04F + 0.16F * hang;
            glow = 0.45F + 0.5F * hang * (0.75F + 0.25F * Mth.sin(age * 2.2F));
        } else {
            tip = target;
            amplitude = 0.0F;
            glow = Math.max(0.5F, 1.2F - (age - cast.slamTick) * 0.06F);
        }
        Vec3 span = tip.subtract(hand);
        double length = span.length();
        if (length < 0.05) {
            return null;
        }
        Vec3 dir = span.scale(1.0 / length);
        Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 lift = side.cross(dir).normalize();
        int n = Math.max(2, (int)Math.ceil(length / STEP));
        Vec3[] points = new Vec3[n + 1];
        for (int k = 0; k <= n; k++) {
            double s = Math.min(1.0, k * STEP / length);
            double bell = Math.sin(s * Math.PI);
            double wave = Math.sin(s * length * 1.6 - age * (hit == null && age >= latch ? 4.0 : 1.4) + i);
            double wave2 = Math.cos(s * length * 1.1 - age * 1.1 + i * 2);
            points[k] = hand.add(span.scale(s)).add(side.scale(amplitude * bell * wave)).add(lift.scale(amplitude * 0.6 * bell * wave2));
        }
        return new Chain(points, (float)length, age * 0.18F + i, glow, own, age + i * 3.0F);
    }

    @SubscribeEvent
    public static void onSubmit(SubmitCustomGeometryEvent event) {
        List<Chain> chains = net.yezon.theabyss.platform.client.renderstate.RenderData.get(event.getLevelRenderState(), CHAINS);
        if (chains == null || chains.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();
        collector.submitCustomGeometry(poseStack, LightTrail.STREAK, (pose, buffer) -> {
            for (Chain chain : chains) {
                int n = chain.points.length;
                float[] width = new float[n];
                float[] light = new float[n];
                for (int k = 0; k < n; k++) {
                    float near = Mth.clamp(((float)chain.points[k].length() - 1.0F) / 3.0F, 0.0F, 1.0F);
                    width[k] = taper(chain, k) * 0.55F;
                    light[k] = chain.glow * 0.55F * (0.25F + 0.75F * near);
                }
                LightTrail.ribbon(pose, buffer, chain.points, width, light, GLOW, Vec3.ZERO);
                spiral(pose, buffer, chain, 0.0);
                spiral(pose, buffer, chain, Math.PI);
                hoops(pose, buffer, chain);
            }
        });
        collector.submitCustomGeometry(poseStack, CHAIN, (pose, buffer) -> {
            for (Chain chain : chains) {
                links(pose, buffer, chain);
            }
        });
    }

    private static Vec3 along(Chain chain, int k) {
        Vec3[] p = chain.points;
        Vec3 t = p[Math.min(k + 1, p.length - 1)].subtract(p[Math.max(k - 1, 0)]);
        return t.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : t.normalize();
    }

    private static Vec3 across(Vec3 tangent) {
        Vec3 ref = Math.abs(tangent.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        return tangent.cross(ref).normalize();
    }

    private static float nearFade(Vec3 p) {
        return Mth.clamp(((float)p.length() - 1.0F) / 3.0F, 0.0F, 1.0F);
    }

    private static void spiral(PoseStack.Pose pose, VertexConsumer buffer, Chain chain, double offset) {
        Vec3[] p = chain.points;
        int n = (p.length - 1) * SPIRAL_STEPS + 1;
        if (n < 3) {
            return;
        }
        Vec3[] points = new Vec3[n];
        float[] width = new float[n];
        float[] light = new float[n];
        for (int j = 0; j < n; j++) {
            int k = Math.min(j / SPIRAL_STEPS, p.length - 2);
            double f = (j - k * SPIRAL_STEPS) / (double)SPIRAL_STEPS;
            Vec3 base = p[k].lerp(p[k + 1], f);
            Vec3 tangent = along(chain, k);
            Vec3 u = across(tangent);
            Vec3 w = tangent.cross(u).normalize();
            double run = (k + f) * STEP;
            double phase = run / SPIRAL_TURN * Math.PI * 2.0 - chain.age * 0.55 + offset;
            float size = taper(chain, k);
            points[j] = base.add(u.scale(Math.cos(phase) * SPIRAL_RADIUS * size)).add(w.scale(Math.sin(phase) * SPIRAL_RADIUS * size));
            width[j] = 0.06F * size;
            light[j] = chain.glow * 0.85F * nearFade(base);
        }
        LightTrail.ribbon(pose, buffer, points, width, light, SPIRAL, Vec3.ZERO);
    }

    private static void hoops(PoseStack.Pose pose, VertexConsumer buffer, Chain chain) {
        Vec3[] p = chain.points;
        if (p.length < 3) {
            return;
        }
        int count = Math.max(2, (int)(chain.length / 1.8F));
        int segments = 18;
        for (int h = 0; h < count; h++) {
            double u = (chain.age * 0.045 + h / (double)count) % 1.0;
            double at = u * (p.length - 1);
            int k = Math.min((int)at, p.length - 2);
            Vec3 centre = p[k].lerp(p[k + 1], at - k);
            Vec3 tangent = along(chain, k);
            Vec3 a = across(tangent);
            Vec3 b = tangent.cross(a).normalize();
            float env = (float)Math.sin(u * Math.PI);
            double radius = (0.2 + 0.1 * env) * taper(chain, k);
            Vec3[] points = new Vec3[segments + 1];
            float[] width = new float[segments + 1];
            float[] light = new float[segments + 1];
            for (int i = 0; i <= segments; i++) {
                double angle = i * Math.PI * 2.0 / segments;
                points[i] = centre.add(a.scale(Math.cos(angle) * radius)).add(b.scale(Math.sin(angle) * radius));
                width[i] = 0.05F * taper(chain, k);
                light[i] = chain.glow * env * nearFade(centre);
            }
            LightTrail.ribbon(pose, buffer, points, width, light, HOOP, Vec3.ZERO);
        }
    }

    private static void links(PoseStack.Pose pose, VertexConsumer buffer, Chain chain) {
        Vec3[] p = chain.points;
        for (int k = 0; k + 1 < p.length; k++) {
            Vec3 a = p[k];
            Vec3 b = p[k + 1];
            Vec3 t = b.subtract(a);
            float len = (float)t.length();
            if (len < 1.0E-4) {
                continue;
            }
            t = t.scale(1.0 / len);
            Vec3 ref = Math.abs(t.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 u = t.cross(ref).normalize();
            Vec3 w = t.cross(u).normalize();
            double c = Math.cos(chain.twist);
            double s = Math.sin(chain.twist);
            float half = WIDTH * 0.5F * taper(chain, k);
            Vec3 u2 = u.scale(c).add(w.scale(s)).scale(half);
            Vec3 w2 = w.scale(c).subtract(u.scale(s)).scale(half);
            float along = Math.min(STEP, chain.length - k * STEP);
            float v0 = (k * STEP % TILE) / TILE;
            float v1 = v0 + Math.max(0.0F, along) / TILE;
            strip(pose, buffer, a, b, u2, 0.0F, 0.5F, v0, v1);
            strip(pose, buffer, a, b, w2, 0.5F, 1.0F, v0, v1);
        }
    }

    private static float taper(Chain chain, int k) {
        return chain.taper ? Mth.clamp(0.3F + k * STEP / 2.5F * 0.7F, 0.3F, 1.0F) : 1.0F;
    }

    private static void strip(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 half, float u0, float u1, float v0, float v1) {
        vertex(buffer, pose, a.subtract(half), u0, v0);
        vertex(buffer, pose, a.add(half), u1, v0);
        vertex(buffer, pose, b.add(half), u1, v1);
        vertex(buffer, pose, b.subtract(half), u0, v1);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, float u, float v) {
        buffer.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
