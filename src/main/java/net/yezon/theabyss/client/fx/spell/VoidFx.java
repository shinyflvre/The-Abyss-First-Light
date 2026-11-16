package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.RingFxClient;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.ring.secondary.VoidRift;

final class VoidFx extends Effect {
    private static final RenderType BLACK = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/white.png"));
    private static final int MAGENTA = 0xE040FF;
    private static final int VIOLET = 0x7A2BFF;
    private static final int PALE = 0xFFC8FF;
    private static final RenderType BODY = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/white.png"));
    private static final float RADIUS = 1.35F;
    private static final float AFTER = 22.0F;
    private static final double TILT = 0.4;
    private static final int LANES = 10;
    private static final int SEGMENTS = 64;

    private final Vec3 centre;
    private final int end;

    VoidFx(SpellFxMessage m) {
        this.centre = SpellFx.vec(m, 0);
        this.end = m.ticks();
        RingFxClient.add(RingFx.LENS, this.centre.x, this.centre.y, this.centre.z, RADIUS * 0.7F, this.end, 0, 0.0F);
    }

    @Override
    public float life() {
        return this.end + AFTER;
    }

    private float radius(float age) {
        if (age >= this.end) {
            return 0.0F;
        }
        float open = (float)Math.min(1.0, age / VoidRift.FORM);
        float overshoot = (float)(1.0 + 0.25 * Math.sin(open * Math.PI) );
        float breathe = 1.0F + 0.05F * (float)Math.sin(age * 0.7);
        float collapse = (float)Math.min(1.0, (this.end - age) / 5.0);
        return RADIUS * (float)easeOut(open) * overshoot * breathe * (float)Math.pow(collapse, 0.6);
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float r = this.radius(age);
        Vec3 c = this.centre;
        if (r > 0.02F) {
            Vec3 rel = f.rel(c);
            float rr = r;
            f.mesh(BLACK, (pose, buffer, cam) -> sphere(pose, buffer, rel, rr));
            Vec3 view = f.camera.subtract(c);
            view = view.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : view.normalize();
            Vec3 right = view.cross(new Vec3(0.0, 1.0, 0.0));
            right = right.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
            Vec3 up = right.cross(view).normalize();
            double precess = age * 0.012;
            Vec3 normal = new Vec3(Math.sin(TILT) * Math.cos(precess), Math.cos(TILT), Math.sin(TILT) * Math.sin(precess)).normalize();
            Vec3 e1 = normal.cross(new Vec3(0.0, 0.0, 1.0)).normalize();
            Vec3 e2 = normal.cross(e1).normalize();
            this.disk(f, c, e1, e2, r, age, view);
            double edgeOn = 1.0 - Math.abs(view.dot(normal));
            this.photonRing(f, c, right, up, r, age, e1, e2, view);
            this.arc(f, c, right, up, r * 1.24, 0.1, Math.PI - 0.1, 0.34F * r, (float)(0.95 * edgeOn), PALE);
            this.arc(f, c, right, up, r * 1.3, -0.05, Math.PI + 0.05, 0.7F * r, (float)(0.5 * edgeOn), MAGENTA);
            this.arc(f, c, right, up, r * 1.16, Math.PI + 0.35, Math.PI * 2.0 - 0.35, 0.14F * r, (float)(0.7 * edgeOn), PALE);
        }
        if (age >= this.end) {
            float i = age - this.end;
            if (i < 6.0F) {
                float k = 1.0F - i / 6.0F;
                f.flare(c, 14.0F * k + 2.0F, MAGENTA, k);
                f.flare(c, 6.0F * k, 0xFFFFFF, k);
            }
            if (i < 16.0F) {
                double e = easeOut(i / 16.0);
                float k = (float)(1.0 - e);
                f.circle(c, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 1.0, 0.0), 5.0 * e, 0.3F * k, k * 0.8F, VIOLET);
                f.circle(c, new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 1.0, 0.0), 5.0 * e, 0.3F * k, k * 0.8F, VIOLET);
            }
        }
    }

    private void disk(Frame f, Vec3 c, Vec3 e1, Vec3 e2, float r, float age, Vec3 view) {
        double inner = r * 1.7;
        double outer = r * 4.7;
        double lane = (outer - inner) / LANES;
        Vec3 rel = f.rel(c);
        Vec3 camera = view;
        Vec3[][] lefts = new Vec3[LANES][];
        Vec3[][] rights = new Vec3[LANES][];
        float[][] lights = new float[LANES][];
        int[][] colours = new int[LANES][];
        for (int k = 0; k < LANES; k++) {
            double u = k / (double)(LANES - 1);
            double rad = inner + lane * (k + 0.5);
            double half = lane * 0.95;
            double spin = age * 0.42 * Math.pow(inner / rad, 1.5);
            int colour = temperature(u);
            float base = (float)(Math.pow(1.0 - u, 1.4) * 0.92 + 0.1);
            Vec3[] left = new Vec3[SEGMENTS + 1];
            Vec3[] right = new Vec3[SEGMENTS + 1];
            float[] light = new float[SEGMENTS + 1];
            int[] rgb = new int[SEGMENTS + 1];
            double seed = k * 2.399;
            for (int i = 0; i <= SEGMENTS; i++) {
                double a = i * Math.PI * 2.0 / SEGMENTS;
                Vec3 dir = e1.scale(Math.cos(a)).add(e2.scale(Math.sin(a)));
                Vec3 tangent = e2.scale(Math.cos(a)).subtract(e1.scale(Math.sin(a)));
                double doppler = 1.0 + 0.7 * tangent.dot(camera);
                double clump = 0.5 + 0.5 * Math.sin(a * 3.0 - spin * 3.0 + seed) * Math.cos(a * 7.0 - spin * 7.0 + seed * 1.7);
                left[i] = rel.add(dir.scale(rad - half));
                right[i] = rel.add(dir.scale(rad + half));
                light[i] = (float)Math.min(1.0, base * doppler * (0.3 + 0.7 * clump));
                rgb[i] = colour;
            }
            lefts[k] = left;
            rights[k] = right;
            lights[k] = light;
            colours[k] = rgb;
        }
        f.mesh(BODY, (pose, buffer, cam) -> {
            for (int k = 0; k < LANES; k++) {
                for (int i = 0; i < SEGMENTS; i++) {
                    Frame.vertex(buffer, pose, lefts[k][i], 0.0F, 0.0F, argb(colours[k][i], lights[k][i]));
                    Frame.vertex(buffer, pose, rights[k][i], 0.0F, 1.0F, argb(colours[k][i], lights[k][i]));
                    Frame.vertex(buffer, pose, rights[k][i + 1], 1.0F, 1.0F, argb(colours[k][i + 1], lights[k][i + 1]));
                    Frame.vertex(buffer, pose, lefts[k][i + 1], 1.0F, 0.0F, argb(colours[k][i + 1], lights[k][i + 1]));
                }
            }
        });
        f.add(LightTrail.SHEET, (pose, buffer) -> {
            for (int k = 0; k < LANES; k++) {
                LightTrail.band(pose, buffer, lefts[k], rights[k], lights[k], colours[k]);
                LightTrail.band(pose, buffer, rights[k], lefts[k], lights[k], colours[k]);
            }
        });
    }

    private static int argb(int rgb, float light) {
        return Math.min(255, (int)(light * 230.0F)) << 24 | rgb;
    }

    private static int temperature(double u) {
        if (u < 0.12) {
            return 0xFFF2FF;
        }
        if (u < 0.3) {
            return PALE;
        }
        if (u < 0.58) {
            return MAGENTA;
        }
        return u < 0.82 ? VIOLET : 0x3A0A60;
    }

    private void photonRing(Frame f, Vec3 c, Vec3 right, Vec3 up, float r, float age, Vec3 e1, Vec3 e2, Vec3 view) {
        int n = 56;
        double rad = r * 1.08;
        Vec3[] p = new Vec3[n + 1];
        float[] w = new float[n + 1];
        float[] l = new float[n + 1];
        for (int i = 0; i <= n; i++) {
            double a = i * Math.PI * 2.0 / n;
            Vec3 dir = right.scale(Math.cos(a)).add(up.scale(Math.sin(a)));
            Vec3 tangent = dir.cross(view);
            p[i] = c.add(dir.scale(rad));
            w[i] = 0.11F * r;
            l[i] = (float)Math.min(1.0, 0.72 + 0.28 * tangent.dot(e1) + 0.05 * Math.sin(a * 5.0 + age * 0.6));
        }
        f.ribbon(p, w, l, 0xFFFFFF);
    }

    private void arc(Frame f, Vec3 c, Vec3 right, Vec3 up, double rad, double from, double to, float width, float light, int rgb) {
        if (light <= 0.01F) {
            return;
        }
        int n = 30;
        Vec3[] p = new Vec3[n + 1];
        float[] w = new float[n + 1];
        float[] l = new float[n + 1];
        for (int i = 0; i <= n; i++) {
            double u = i / (double)n;
            double a = from + (to - from) * u;
            float env = (float)Math.sin(u * Math.PI);
            p[i] = c.add(right.scale(Math.cos(a) * rad)).add(up.scale(Math.sin(a) * rad));
            w[i] = width * (0.35F + 0.65F * env);
            l[i] = light * env;
        }
        f.ribbon(p, w, l, rgb);
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        if (age < this.end - 3) {
            for (int k = 0; k < 4; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = 4.0 + random.nextDouble() * (VoidRift.PULL_RADIUS - 4.0);
                Vec3 out = new Vec3(Math.cos(a), (random.nextDouble() - 0.5) * 0.25, Math.sin(a));
                Vec3 at = this.centre.add(out.scale(d));
                int life = 12 + random.nextInt(8);
                Vec3 swirl = new Vec3(-Math.sin(a), 0.0, Math.cos(a)).scale(d * 0.05);
                Vec3 v = out.scale(-d / life).add(swirl);
                emit(level, sparkle(k % 2 == 0 ? MAGENTA : PALE, 0.11F, life).drag(1.0F), at, v);
            }
        }
        if (age >= this.end && age < this.end + 1.0F) {
            for (int k = 0; k < 50; k++) {
                Vec3 dir = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                emit(level, sparkle(k % 3 == 0 ? 0xFFFFFF : MAGENTA, 0.16F, 26).drag(0.9F), this.centre, dir.scale(0.6 + random.nextDouble() * 0.4));
            }
            for (int k = 0; k < 24; k++) {
                Vec3 dir = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.5, random.nextGaussian()).normalize();
                emit(level, wisp(k % 2 == 0 ? 0x7A2BFF : 0x3A0A60, 0.35F, 22).drag(0.9F), this.centre, dir.scale(0.45));
            }
        }
    }

    private static void sphere(PoseStack.Pose pose, VertexConsumer buffer, Vec3 c, float r) {
        int rings = 12;
        int segments = 20;
        int black = 0xFF000000;
        for (int i = 0; i < rings; i++) {
            double t0 = Math.PI * i / rings;
            double t1 = Math.PI * (i + 1) / rings;
            for (int j = 0; j < segments; j++) {
                double p0 = Math.PI * 2.0 * j / segments;
                double p1 = Math.PI * 2.0 * (j + 1) / segments;
                Frame.quad(buffer, pose, point(c, r, t0, p0), point(c, r, t1, p0), point(c, r, t1, p1), point(c, r, t0, p1), 0.0F, 0.0F, 1.0F, 1.0F, black);
            }
        }
    }

    private static Vec3 point(Vec3 c, float r, double theta, double phi) {
        return c.add(r * Math.sin(theta) * Math.cos(phi), r * Math.cos(theta), r * Math.sin(theta) * Math.sin(phi));
    }
}
