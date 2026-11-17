package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.renderer.LightTrail;

public final class Frame {
    public final Minecraft mc;
    public final ClientLevel level;
    public final Vec3 camera;
    public final float pt;
    final Map<RenderType, List<BiConsumer<PoseStack.Pose, VertexConsumer>>> draws = new LinkedHashMap<>();

    Frame(Minecraft mc, ClientLevel level, Vec3 camera, float pt) {
        this.mc = mc;
        this.level = level;
        this.camera = camera;
        this.pt = pt;
    }

    public Vec3 rel(Vec3 world) {
        return world.subtract(this.camera);
    }

    public Vec3 pos(int id) {
        Entity e = this.level.getEntity(id);
        return e == null ? null : e.getPosition(this.pt);
    }

    public Vec3 body(int id) {
        Entity e = this.level.getEntity(id);
        return e == null ? null : e.getPosition(this.pt).add(0.0, e.getBbHeight() * 0.5, 0.0);
    }

    public void add(RenderType type, BiConsumer<PoseStack.Pose, VertexConsumer> draw) {
        this.draws.computeIfAbsent(type, k -> new ArrayList<>()).add(draw);
    }

    public void ribbon(Vec3[] points, float[] width, float[] light, int rgb) {
        this.ribbon(LightTrail.STREAK, points, width, light, rgb);
    }

    public void ribbon(RenderType type, Vec3[] points, float[] width, float[] light, int rgb) {
        if (points.length < 2) {
            return;
        }
        Vec3[] rel = new Vec3[points.length];
        for (int i = 0; i < points.length; i++) {
            rel[i] = this.rel(points[i]);
        }
        float[] w = width.clone();
        float[] l = light.clone();
        this.add(type, (pose, buffer) -> LightTrail.ribbon(pose, buffer, rel, w, l, rgb, Vec3.ZERO));
    }

    public void ribbon(Vec3[] points, float width, float light, int rgb) {
        float[] w = new float[points.length];
        float[] l = new float[points.length];
        java.util.Arrays.fill(w, width);
        java.util.Arrays.fill(l, light);
        this.ribbon(points, w, l, rgb);
    }

    public void groundBand(Vec3[] points, float width, float light, int rgb) {
        int n = points.length;
        if (n < 2) {
            return;
        }
        Vec3[] left = new Vec3[n];
        Vec3[] right = new Vec3[n];
        float[] l = new float[n];
        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            Vec3 tangent = points[Math.min(i + 1, n - 1)].subtract(points[Math.max(i - 1, 0)]);
            Vec3 side = new Vec3(-tangent.z, 0.0, tangent.x);
            side = side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize().scale(width * 0.5);
            left[i] = this.rel(points[i].add(side));
            right[i] = this.rel(points[i].subtract(side));
            l[i] = light;
            c[i] = rgb;
        }
        this.add(LightTrail.SHEET, (pose, buffer) -> {
            LightTrail.band(pose, buffer, left, right, l, c);
            LightTrail.band(pose, buffer, right, left, l, c);
        });
    }

    public void line(Vec3 a, Vec3 b, float width, float light, int rgb) {
        int n = Math.max(2, (int)Math.ceil(a.distanceTo(b) / 1.5));
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            p[i] = a.lerp(b, i / (double)n);
        }
        this.ribbon(p, width, light, rgb);
    }

    public static Vec3[] curve(Vec3 a, Vec3 control, Vec3 b, int n) {
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            double t = i / (double)n;
            double u = 1.0 - t;
            p[i] = a.scale(u * u).add(control.scale(2.0 * u * t)).add(b.scale(t * t));
        }
        return p;
    }

    public void circle(Vec3 centre, Vec3 u, Vec3 v, double radius, float width, float light, int rgb) {
        int n = Math.max(12, (int)(radius * 10.0));
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            double a = i * Math.PI * 2.0 / n;
            p[i] = centre.add(u.scale(Math.cos(a) * radius)).add(v.scale(Math.sin(a) * radius));
        }
        this.ribbon(p, width, light, rgb);
    }

    public void ground(Vec3 centre, double radius, float width, float light, int rgb) {
        this.circle(centre, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), radius, width, light, rgb);
    }

    public void flare(Vec3 at, float size, int rgb, float light) {
        Vec3 rel = this.rel(at);
        this.add(LightTrail.FLARE, (pose, buffer) -> LightTrail.flare(pose, buffer, rel, size, rgb, light, Vec3.ZERO));
    }

    public void zap(Vec3 a, Vec3 b, double jag, float width, float light, int rgb, net.minecraft.util.RandomSource random) {
        Vec3 axis = b.subtract(a);
        double length = axis.length();
        int n = Math.max(3, (int)(length / 0.5));
        Vec3 s1 = axis.cross(new Vec3(0.0, 1.0, 0.0));
        s1 = s1.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : s1.normalize();
        Vec3 s2 = axis.cross(s1).normalize();
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            double f = i / (double)n;
            double env = Math.sin(f * Math.PI);
            p[i] = i == 0 || i == n ? a.lerp(b, f) : a.lerp(b, f).add(s1.scale((random.nextDouble() - 0.5) * 2.0 * jag * env))
                .add(s2.scale((random.nextDouble() - 0.5) * 2.0 * jag * env));
        }
        this.ribbon(p, width, light, rgb);
        this.ribbon(p, width * 0.3F, light, 0xFFFFFF);
    }

    public void mesh(RenderType type, Painter painter) {
        Vec3 cam = this.camera;
        this.add(type, (pose, buffer) -> painter.paint(pose, buffer, cam));
    }

    @FunctionalInterface
    public interface Painter {
        void paint(PoseStack.Pose pose, VertexConsumer buffer, Vec3 camera);
    }

    public static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, float u, float v, int argb) {
        buffer.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(argb).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static void quad(VertexConsumer buffer, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float u0, float v0, float u1, float v1, int argb) {
        vertex(buffer, pose, a, u0, v1, argb);
        vertex(buffer, pose, b, u1, v1, argb);
        vertex(buffer, pose, c, u1, v0, argb);
        vertex(buffer, pose, d, u0, v0, argb);
    }
}
