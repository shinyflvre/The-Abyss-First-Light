package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.MeteorShower;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.SpellCircle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class MeteorShowerFx extends Effect {
    static final RenderType ROCK = RenderTypes.entityCutout(texture("void_rock"));
    static final RenderType ROCK_GLOW = RenderTypes.eyes(texture("void_rock_glow"));
    private static final RenderType CRYSTAL = RenderTypes.entityCutout(texture("void_crystal"));
    private static final RenderType RIFT = RenderTypes.entityTranslucent(texture("sky_rift_disc"));
    static final RenderType[] TRAIL = frames("meteor_trail_0", 4);
    static final RenderType[] HEAD = frames("meteor_head_0", 4);
    private static final float AFTER = 70.0F;
    private static final int RIFT_FRAMES = 6;
    private static final int RING = 24;
    private static final int STREAKS = 14;
    private static final int RISERS = 160;

    private final Vec3 rift;
    private final Vec3 lean;
    private final Rock[] rocks;
    private final List<Streak> streaks = new ArrayList<>();
    private final List<Riser> risers = new ArrayList<>();
    private final float end;

    MeteorShowerFx(SpellFxMessage m) {
        List<Double> d = m.data();
        this.rift = SpellFx.vec(m, 0);
        this.lean = new Vec3(d.get(3), 0.0, d.get(4));
        int count = Math.max(0, (d.size() - MeteorShower.HEADER) / MeteorShower.ROCK);
        RandomSource random = RandomSource.create((long)(this.rift.x * 31.0 + this.rift.z * 17.0 + this.rift.y));
        this.rocks = new Rock[count];
        float last = 0.0F;
        Vec3 slant = Vec3.ZERO;
        for (int i = 0; i < count; i++) {
            int o = MeteorShower.HEADER + i * MeteorShower.ROCK;
            Rock r = new Rock();
            r.from = SpellFx.vec(m, o);
            r.to = SpellFx.vec(m, o + 3);
            r.launch = d.get(o + 6).floatValue();
            r.fall = Math.max(1.0F, d.get(o + 7).floatValue());
            r.big = d.get(o + 8) > 0.5;
            Vec3 path = r.to.subtract(r.from);
            r.dir = path.lengthSqr() < 1.0E-6 ? new Vec3(0.0, -1.0, 0.0) : path.normalize();
            r.half = r.big ? 0.8F : 0.35F + random.nextFloat() * 0.1F;
            Vector3f spinAxis = new Vector3f(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F);
            r.spinAxis = spinAxis.lengthSquared() < 1.0E-4F ? new Vector3f(0.0F, 1.0F, 0.0F) : spinAxis.normalize();
            r.spinRate = 0.22F + random.nextFloat() * 0.2F;
            r.cracks = cracks(r.to, r.big ? 9 : 6, r.big ? 3.2 : 1.5, r.big ? 2.0 : 1.0, random);
            r.crystals = crystals(r, random);
            this.rocks[i] = r;
            last = Math.max(last, r.impact());
            slant = slant.add(r.dir);
            float radius = (float)(r.big ? MeteorShower.BIG_RADIUS : MeteorShower.SMALL_RADIUS);
            SpellFx.add(new SpellCircleFx(SpellCircle.CLOSING, r.to, -1, MeteorShower.VIOLET, radius, r.fall + 3.0F, r.launch));
        }
        this.end = last + AFTER;
        Vec3 dir = slant.lengthSqr() < 1.0E-6 ? new Vec3(0.0, -1.0, 0.0) : slant.normalize();
        for (int i = 0; i < STREAKS; i++) {
            Streak s = new Streak();
            s.start = 5.0F + random.nextFloat() * (MeteorShower.CLOSE_START - 8.0F);
            s.life = 8.0F + random.nextFloat() * 5.0F;
            s.length = 3.0 + random.nextDouble() * 2.0;
            s.distance = 6.0 + random.nextDouble() * 6.0;
            s.from = this.rift.add(disc(random, 0.85)).add(0.0, -0.4, 0.0);
            Vec3 jitter = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.3, random.nextGaussian()).scale(0.12);
            s.dir = dir.add(jitter).normalize();
            this.streaks.add(s);
        }
    }

    private static Vec3 disc(RandomSource random, double inner) {
        double a = random.nextDouble() * Math.PI * 2.0;
        double d = Math.sqrt(random.nextDouble()) * MeteorShower.RIFT_RADIUS * inner;
        return new Vec3(Math.cos(a) * d, 0.0, Math.sin(a) * d);
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/fx/meteoroid/" + name + ".png");
    }

    private static RenderType[] frames(String prefix, int count) {
        RenderType[] out = new RenderType[count];
        for (int i = 0; i < count; i++) {
            out[i] = LightTrail.type(texture(prefix + i));
        }
        return out;
    }

    @Override
    public float life() {
        return this.end;
    }

    private static float open(float age) {
        if (age < MeteorShower.OPEN) {
            return (float)smooth(age / MeteorShower.OPEN);
        }
        if (age < MeteorShower.CLOSE_START) {
            return 1.0F;
        }
        return 1.0F - (float)smooth((age - MeteorShower.CLOSE_START) / (double)(MeteorShower.CLOSE_END - MeteorShower.CLOSE_START));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        this.drawRift(f, age);
        for (Streak s : this.streaks) {
            this.drawStreak(f, s, age);
        }
        for (Rock r : this.rocks) {
            this.drawRock(f, r, age);
            this.drawImpact(f, r, age);
        }
        for (Riser s : this.risers) {
            float u = Math.min(1.0F, (s.age + f.pt) / s.life);
            float head = (1.0F - (1.0F - u) * (1.0F - u)) * s.height;
            float tail = Math.max(0.0F, head - s.height * 0.5F);
            float a = Math.min(1.0F, u * 6.0F) * Math.min(1.0F, (1.0F - u) * 3.0F);
            Vec3 top = s.base.add(0.0, head, 0.0);
            Vec3 low = s.base.add(0.0, tail, 0.0);
            f.ribbon(new Vec3[] {low, low.lerp(top, 0.6), top}, new float[] {0.012F, 0.05F, 0.07F}, new float[] {0.0F, a * 0.55F, a}, s.rgb);
            f.flare(top, 0.2F, s.rgb, a * 0.85F);
        }
    }

    private void rise(Vec3 base, int rgb, float height, RandomSource random) {
        if (this.risers.size() >= RISERS) {
            return;
        }
        Riser s = new Riser();
        s.base = base;
        s.rgb = rgb;
        s.height = height;
        s.life = 12 + random.nextInt(14);
        this.risers.add(s);
    }

    private void drawRift(Frame f, float age) {
        if (age >= MeteorShower.CLOSE_END) {
            return;
        }
        float o = open(age);
        if (o <= 0.001F) {
            return;
        }
        double radius = MeteorShower.RIFT_RADIUS * (0.08 + 0.92 * o);
        double turn = age * 0.039;
        Vec3 e1 = new Vec3(Math.cos(turn), 0.0, Math.sin(turn)).scale(radius);
        Vec3 e2 = new Vec3(-Math.sin(turn), 0.0, Math.cos(turn)).scale(radius);
        Vec3 a = f.rel(this.rift.subtract(e1).subtract(e2));
        Vec3 b = f.rel(this.rift.add(e1).subtract(e2));
        Vec3 c = f.rel(this.rift.add(e1).add(e2));
        Vec3 d = f.rel(this.rift.subtract(e1).add(e2));
        int frame = (int)(age / 3.0F) % RIFT_FRAMES;
        float v0 = frame / (float)RIFT_FRAMES;
        float v1 = (frame + 1) / (float)RIFT_FRAMES;
        int alpha = (int)(255.0F * Mth.clamp(o * 1.6F, 0.0F, 1.0F));
        int argb = alpha << 24 | 0xFFFFFF;
        f.mesh(RIFT, (pose, buffer, cam) -> Frame.quad(buffer, pose, a, b, c, d, 0.0F, v0, 1.0F, v1, argb));
        Vec3 under = this.rift.add(0.0, -0.3, 0.0);
        f.flare(under, (float)(radius * 1.4), MeteorShower.DEEP, 0.22F * o);
        f.flare(under, (float)(radius * 0.7), MeteorShower.VIOLET, 0.2F * o);
        MagicLights.add(under, MeteorShower.VIOLET, 20.0F, 0.8F * o);
        for (Rock r : this.rocks) {
            float k = age - r.launch;
            if (k >= -2.0F && k < 7.0F) {
                float fade = k < 0.0F ? (k + 2.0F) / 2.0F : 1.0F - k / 7.0F;
                Vec3 at = r.from.add(0.0, -0.4, 0.0);
                f.flare(at, (r.big ? 11.0F : 6.0F) * fade, MeteorShower.LIGHT, fade);
                f.flare(at, (r.big ? 4.0F : 2.2F) * fade, 0xFFFFFF, fade);
                if (k >= 0.0F) {
                    double ring = (r.big ? 1.2 : 0.6) + k * (r.big ? 0.7 : 0.4);
                    Vec3[] p = new Vec3[RING + 1];
                    for (int j = 0; j <= RING; j++) {
                        double t = j * Math.PI * 2.0 / RING;
                        p[j] = r.from.add(Math.cos(t) * ring, -0.15, Math.sin(t) * ring);
                    }
                    f.ribbon(p, r.big ? 0.35F : 0.22F, fade, MeteorShower.LIGHT);
                }
            }
        }
    }

    private void drawStreak(Frame f, Streak s, float age) {
        float k = age - s.start;
        if (k < 0.0F || k > s.life) {
            return;
        }
        double t = k / s.life;
        Vec3 head = s.from.add(s.dir.scale(s.distance * t));
        double length = Math.min(s.length, s.distance * t + 0.2);
        float fade = t > 0.66 ? (float)((1.0 - t) / 0.34) : 1.0F;
        int n = 6;
        Vec3[] p = new Vec3[n];
        float[] w = new float[n];
        float[] l = new float[n];
        for (int j = 0; j < n; j++) {
            double q = j / (double)(n - 1);
            p[j] = head.subtract(s.dir.scale(length * q));
            w[j] = (float)(0.25 * (1.0 - q) + 0.03);
            l[j] = (float)(1.0 - q) * fade;
        }
        f.ribbon(TRAIL[(int)(age / 2.0F) % TRAIL.length], p, w, l, 0xFFFFFF);
    }

    private void drawRock(Frame f, Rock r, float age) {
        float k = age - r.launch;
        if (k < 0.0F || k >= r.fall) {
            return;
        }
        Vec3 at = r.at(age);
        float grow = Math.min(1.0F, (k + 0.5F) / 2.0F);
        float cube = r.half * 2.0F;
        double travelled = at.distanceTo(r.from);
        double length = Math.min(travelled + 0.5, r.big ? 16.0 : 13.0);
        int n = 20;
        Vec3[] p = new Vec3[n];
        float[] w = new float[n];
        float[] l = new float[n];
        for (int j = 0; j < n; j++) {
            double s = j / (double)(n - 1);
            p[j] = at.subtract(r.dir.scale(length * s));
            w[j] = (float)(2.4 * cube * Math.pow(1.0 - s, 0.8) + 0.2) * grow;
            l[j] = Mth.clamp((float)(1.3 - s), 0.0F, 1.0F);
        }
        f.ribbon(TRAIL[(int)(age / 2.0F) % TRAIL.length], p, w, l, 0xFFFFFF);
        int nb = 9;
        Vec3[] pb = new Vec3[nb];
        float[] wb = new float[nb];
        float[] lb = new float[nb];
        for (int j = 0; j < nb; j++) {
            double s = 0.4 * j / (nb - 1);
            pb[j] = at.subtract(r.dir.scale(length * s));
            wb[j] = (float)(0.3 * (2.4 * cube * Math.pow(1.0 - s, 0.8) + 0.2)) * grow;
            lb[j] = (float)Math.pow(Math.max(0.0, 1.0 - s / 0.4), 1.2);
        }
        f.ribbon(LightTrail.STREAK, pb, wb, lb, MeteorShower.PALE);
        Quaternionf spin = new Quaternionf().rotateAxis(age * r.spinRate, r.spinAxis.x, r.spinAxis.y, r.spinAxis.z);
        int frame = (int)(age / 3.0F) % 3;
        Vec3 rel = f.rel(at);
        float half = r.half * grow;
        f.mesh(ROCK, (pose, buffer, cam) -> cube(pose, buffer, rel, half, spin, frame));
        f.mesh(ROCK_GLOW, (pose, buffer, cam) -> cube(pose, buffer, rel, half * 1.02F, spin, frame));
        Vec3 toCamera = f.camera.subtract(at);
        Vec3 glow = toCamera.lengthSqr() < 1.0E-4 ? at : at.add(toCamera.normalize().scale(half * 1.8));
        Vec3 glowRel = f.rel(glow);
        float size = 3.5F * cube * grow;
        f.add(HEAD[(int)age % HEAD.length], (pose, buffer) -> LightTrail.flare(pose, buffer, glowRel, size, 0xFFFFFF, 0.85F, Vec3.ZERO));
        MagicLights.add(at, MeteorShower.VIOLET, r.big ? 14.0F : 9.0F, grow);
    }

    private void drawImpact(Frame f, Rock r, float age) {
        float i = age - r.impact();
        if (i < 0.0F) {
            return;
        }
        boolean big = r.big;
        if (i < 4.0F) {
            float k = 1.0F - i / 4.0F;
            f.flare(r.to.add(0.0, big ? 1.2 : 0.7, 0.0), (big ? 12.0F : 6.0F) * (0.5F + 0.5F * k), MeteorShower.LIGHT, k);
            f.flare(r.to.add(0.0, big ? 1.0 : 0.6, 0.0), big ? 7.0F : 3.0F, 0xFFFFFF, k * k);
        }
        if (i < 10.0F) {
            float k = 1.0F - i / 10.0F;
            double height = (big ? 10.0 : 5.0) * easeOut(i / 3.0);
            f.line(r.to, r.to.add(0.0, height, 0.0), (big ? 2.6F : 1.2F) * k, k, MeteorShower.VIOLET);
            f.line(r.to, r.to.add(0.0, height * 0.7, 0.0), (big ? 0.9F : 0.45F) * k, k, MeteorShower.PALE);
        }
        if (i < 8.0F) {
            MagicLights.add(r.to.add(0.0, 1.0, 0.0), MeteorShower.LIGHT, big ? 10.0F : 7.0F, (1.0F - i / 8.0F) * (big ? 0.4F : 0.3F));
        }
        float glow = Math.max(0.0F, 1.0F - i / AFTER);
        float flicker = 0.75F + 0.25F * (float)Math.sin(age * 1.3 + r.launch);
        for (Vec3[] crack : r.cracks) {
            f.ribbon(crack, big ? 0.16F : 0.11F, glow * flicker, MeteorShower.VIOLET);
            f.ribbon(crack, big ? 0.06F : 0.045F, glow * flicker, MeteorShower.PALE);
        }
        if (i < MeteorShower.SHATTER) {
            float grow = (float)easeOut(Math.min(1.0, i / 3.0));
            for (Crystal c : r.crystals) {
                this.drawCrystal(f, c, grow);
                Vec3 tip = c.base.add(c.dir.scale(c.height * grow));
                f.flare(tip, big ? 1.6F : 1.1F, MeteorShower.SOFT, 0.55F * grow);
            }
        }
    }

    private void drawCrystal(Frame f, Crystal c, float grow) {
        float height = c.height * grow;
        if (height < 0.05F) {
            return;
        }
        List<Float> cuts = new ArrayList<>();
        cuts.add(0.0F);
        for (int b = 1; b < height; b++) {
            cuts.add((float)b);
        }
        cuts.add(height);
        Vec3 base = f.rel(c.base);
        float radius = c.radius * (0.6F + 0.4F * grow);
        f.mesh(CRYSTAL, (pose, buffer, cam) -> {
            for (int s = 0; s + 1 < cuts.size(); s++) {
                float y0 = cuts.get(s);
                float y1 = cuts.get(s + 1);
                if (y1 - y0 < 1.0E-3F) {
                    continue;
                }
                float r0 = radius * (1.0F - y0 / height);
                float r1 = radius * (1.0F - y1 / height);
                int block = (int)Math.floor(y0 + 1.0E-4F);
                float vt = 1.0F - (y1 - block);
                float vb = 1.0F - (y0 - block);
                for (int j = 0; j < 6; j++) {
                    double a0 = c.turn + j * Math.PI / 3.0;
                    double a1 = c.turn + (j + 1) * Math.PI / 3.0;
                    Vec3 bl = c.point(base, y0, a0, r0);
                    Vec3 br = c.point(base, y0, a1, r0);
                    Vec3 tr = c.point(base, y1, a1, r1);
                    Vec3 tl = c.point(base, y1, a0, r1);
                    float u0 = (j * 0.27F) % 0.5F;
                    float u1 = Math.min(1.0F, u0 + Math.max(0.12F, Math.max(r0, r1)));
                    quadUv(buffer, pose, bl, br, tr, tl, u0, vt, u1, vb);
                }
            }
        });
    }

    private static void quadUv(VertexConsumer buffer, PoseStack.Pose pose, Vec3 bl, Vec3 br, Vec3 tr, Vec3 tl, float u0, float vTop, float u1, float vBottom) {
        Frame.vertex(buffer, pose, bl, u0, vBottom, 0xFFFFFFFF);
        Frame.vertex(buffer, pose, br, u1, vBottom, 0xFFFFFFFF);
        Frame.vertex(buffer, pose, tr, u1, vTop, 0xFFFFFFFF);
        Frame.vertex(buffer, pose, tl, u0, vTop, 0xFFFFFFFF);
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        this.risers.removeIf(s -> ++s.age >= s.life);
        float o = open(age);
        if (o > 0.15F && age < MeteorShower.CLOSE_END) {
            double scale = 0.08 + 0.92 * o;
            for (int k = 0; k < 3; k++) {
                Vec3 p = this.rift.add(disc(random, 0.95 * scale)).add(0.0, -0.3 - random.nextDouble() * 0.6, 0.0);
                emit(level, spark(0.18F + random.nextFloat() * 0.08F, 12), p, Vec3.ZERO);
            }
            if (random.nextInt(2) == 0) {
                Vec3 p = this.rift.add(disc(random, 0.8 * scale)).add(0.0, -0.6, 0.0);
                emit(level, flame(0.45F, 14).gravity(0.004F), p, new Vec3(0.0, -0.05, 0.0));
            }
        }
        for (Rock r : this.rocks) {
            float k = age - r.launch;
            if (k >= 0.0F && k < r.fall) {
                this.trail(level, r, age, random);
            }
            if (age >= r.impact() && !r.burst) {
                r.burst = true;
                this.burst(level, r, random);
            }
            if (r.burst && !r.shattered && age >= r.impact() + MeteorShower.SHATTER) {
                r.shattered = true;
                this.shatter(level, r, random);
            }
            float since = age - r.impact();
            if (r.burst && since < 40.0F) {
                double reach = r.big ? 6.0 : 2.8;
                int rate = r.big ? 3 : 1;
                for (int j = 0; j < rate; j++) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double d = reach * Math.sqrt(random.nextDouble());
                    this.rise(r.to.add(Math.cos(a) * d, 0.05, Math.sin(a) * d), j % 2 == 0 ? MeteorShower.VIOLET : MeteorShower.SOFT,
                        1.4F + random.nextFloat() * (r.big ? 2.6F : 1.6F), random);
                }
            }
            if (r.burst && age < r.impact() + AFTER - 10.0F) {
                double spread = r.big ? 2.2 : 1.2;
                if (random.nextInt(2) == 0) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double dist = Math.sqrt(random.nextDouble()) * spread;
                    emit(level, flame(0.35F + random.nextFloat() * 0.2F, 12).flags(FxOptions.SHRINK), r.to.add(Math.cos(a) * dist, 0.15, Math.sin(a) * dist),
                        new Vec3(0.0, 0.04, 0.0));
                }
                if (random.nextInt(5) == 0) {
                    emit(level, smoke(MeteorShower.SMOKE, r.big ? 1.0F : 0.7F, 40).gravity(-0.008F),
                        r.to.add(random.nextGaussian() * spread * 0.5, 0.5, random.nextGaussian() * spread * 0.5), new Vec3(0.0, 0.04, 0.0));
                }
            }
        }
    }

    private void trail(ClientLevel level, Rock r, float age, RandomSource random) {
        Vec3 at = r.at(age);
        Vec3 prev = r.at(Math.max(r.launch, age - 1.0F));
        Vec3 velocity = at.subtract(prev);
        float cube = r.half * 2.0F;
        Vec3 side = r.dir.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 lift = side.cross(r.dir);
        TrailStreaks.emit(prev, at, MeteorShower.VIOLET, r.big ? 5 : 3);
        int outer = r.big ? 4 : 2;
        for (int k = 0; k < outer; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double reach = cube * (0.9 + random.nextDouble() * 0.8);
            Vec3 off = side.scale(Math.cos(a) * reach).add(lift.scale(Math.sin(a) * reach));
            TrailStreaks.emit(prev.add(off), at.add(off), k % 2 == 0 ? MeteorShower.VIOLET : MeteorShower.SOFT, 1);
        }
        int tongues = r.big ? 4 : 2 + random.nextInt(2);
        for (int t = 0; t < tongues; t++) {
            Vec3 off = side.scale((random.nextDouble() - 0.5) * 0.6 * cube).add(lift.scale((random.nextDouble() - 0.5) * 0.6 * cube));
            Vec3 p = at.subtract(r.dir.scale(cube * (0.3 + random.nextDouble() * 0.9))).add(off);
            Vec3 v = velocity.scale(0.1).add(random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
            emit(level, flame(cube * (0.6F + random.nextFloat() * 0.4F), 6 + random.nextInt(5)).flags(FxOptions.SHRINK).drag(0.9F), p, v);
        }
        double travelled = at.distanceTo(r.from);
        double length = Math.min(travelled + 0.5, r.big ? 16.0 : 10.0 + r.half * 8.0);
        int sparks = 1 + random.nextInt(2);
        for (int s = 0; s < sparks; s++) {
            double q = 0.6 + random.nextDouble() * 0.4;
            Vec3 p = at.subtract(r.dir.scale(length * q)).add(side.scale((random.nextDouble() - 0.5) * cube));
            emit(level, spark(0.16F + random.nextFloat() * 0.06F, 10 + random.nextInt(3)), p, Vec3.ZERO);
        }
        if (random.nextInt(3) == 0) {
            emit(level, smoke(MeteorShower.SMOKE, cube * 0.7F, 12 + random.nextInt(8)).gravity(-0.004F).drag(0.92F),
                at.subtract(r.dir.scale(2.0 * cube)), new Vec3(0.0, 0.02, 0.0));
        }
    }

    private void burst(ClientLevel level, Rock r, RandomSource random) {
        Vec3 c = r.to.add(0.0, 0.3, 0.0);
        boolean big = r.big;
        int sparkles = big ? 70 : 28;
        for (int k = 0; k < sparkles; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.2 + random.nextDouble() * (big ? 0.6 : 0.35);
            int colour = k % 3 == 0 ? 0xFFFFFF : k % 3 == 1 ? MeteorShower.PALE : MeteorShower.LIGHT;
            emit(level, sparkle(colour, 0.13F, 36).gravity(0.05F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE), c,
                new Vec3(Math.cos(a) * s, 0.3 + random.nextDouble() * (big ? 0.6 : 0.4), Math.sin(a) * s));
        }
        int flames = big ? 24 : 12;
        for (int k = 0; k < flames; k++) {
            double a = k * Math.PI * 2.0 / flames;
            double s = big ? 0.7 : 0.4;
            emit(level, flame(big ? 0.7F : 0.5F, 10).flags(FxOptions.SHRINK).drag(0.88F), c.add(Math.cos(a) * 0.6, 0.0, Math.sin(a) * 0.6),
                new Vec3(Math.cos(a) * s, 0.05, Math.sin(a) * s));
        }
        int smokes = big ? 8 : 4;
        double spread = big ? 1.4 : 0.7;
        for (int k = 0; k < smokes; k++) {
            emit(level, smoke(MeteorShower.SMOKE, (big ? 0.7F : 0.45F) + random.nextFloat() * (big ? 0.3F : 0.25F), 45).gravity(-0.008F).drag(0.93F),
                c.add(random.nextGaussian() * spread, random.nextDouble() * 0.8, random.nextGaussian() * spread),
                new Vec3(random.nextGaussian() * 0.04, 0.06 + random.nextDouble() * 0.05, random.nextGaussian() * 0.04));
        }
        int shards = big ? 20 : 8;
        for (int k = 0; k < shards; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double s = 0.1 + random.nextDouble() * (big ? 0.35 : 0.2);
            emit(level, shard(0.2F + random.nextFloat() * 0.1F, 30).gravity(0.06F).drag(0.96F).flags(FxOptions.BOUNCE), c,
                new Vec3(Math.cos(a) * s, 0.4 + random.nextDouble() * 0.5, Math.sin(a) * s));
        }
    }

    private void shatter(ClientLevel level, Rock r, RandomSource random) {
        for (Crystal c : r.crystals) {
            Vec3 out = c.base.subtract(r.to).multiply(1.0, 0.0, 1.0);
            out = out.lengthSqr() < 1.0E-4 ? new Vec3(random.nextGaussian(), 0.0, random.nextGaussian()).normalize() : out.normalize();
            for (int k = 0; k < 8; k++) {
                Vec3 p = c.base.add(c.dir.scale(random.nextDouble() * c.height));
                Vec3 v = out.scale(0.08 + random.nextDouble() * 0.15).add(random.nextGaussian() * 0.04, 0.12 + random.nextDouble() * 0.2, random.nextGaussian() * 0.04);
                emit(level, shard(0.2F + random.nextFloat() * 0.12F, 26 + random.nextInt(10)).gravity(0.05F).drag(0.95F).flags(FxOptions.BOUNCE), p, v);
            }
            for (int k = 0; k < 2; k++) {
                emit(level, sparkle(MeteorShower.PALE, 0.12F, 20).flags(FxOptions.FLICKER | FxOptions.HOT), c.base.add(c.dir.scale(c.height * random.nextDouble())),
                    new Vec3(random.nextGaussian() * 0.05, 0.05, random.nextGaussian() * 0.05));
            }
        }
    }

    static FxOptions flame(float size, int life) {
        return TheabyssModParticleTypes.SP_VOID_FLAME.get().of(0xFFFFFF, size, life, 0);
    }

    static FxOptions shard(float size, int life) {
        return TheabyssModParticleTypes.SP_VOID_SHARD.get().of(0xFFFFFF, size, life, 0);
    }

    static FxOptions spark(float size, int life) {
        return TheabyssModParticleTypes.SP_TRAIL_SPARK.get().of(0xFFFFFF, size, life, FxOptions.ANIMATE);
    }

    private static Vec3[][] cracks(Vec3 centre, int count, double length, double inner, RandomSource random) {
        Vec3[][] out = new Vec3[count][];
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count + random.nextDouble() * 0.5;
            double reach = length * (0.6 + random.nextDouble() * 0.6);
            int n = 6;
            Vec3[] p = new Vec3[n + 1];
            for (int k = 0; k <= n; k++) {
                double r = inner * 0.4 + reach * k / n;
                double wobble = (random.nextDouble() - 0.5) * 0.45;
                p[k] = centre.add(Math.cos(a + wobble / Math.max(0.3, r)) * r, 0.06, Math.sin(a + wobble / Math.max(0.3, r)) * r);
            }
            out[i] = p;
        }
        return out;
    }

    private static Crystal[] crystals(Rock r, RandomSource random) {
        int count = r.big ? 5 : 2 + random.nextInt(2);
        Crystal[] out = new Crystal[count];
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count + random.nextDouble() * 0.9;
            double d = r.big ? 0.6 + random.nextDouble() * 1.6 : 0.3 + random.nextDouble() * 0.8;
            Crystal c = new Crystal();
            c.base = r.to.add(Math.cos(a) * d, -0.25, Math.sin(a) * d);
            double tilt = 0.3 + random.nextDouble() * 0.3;
            Vec3 out2 = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            c.dir = out2.scale(Math.sin(tilt)).add(0.0, Math.cos(tilt), 0.0).normalize();
            c.height = r.big ? (i == 0 ? 2.5F : 1.7F + random.nextFloat() * 0.6F) : 1.5F + random.nextFloat() * 0.5F;
            c.radius = r.big ? 0.3F + random.nextFloat() * 0.05F : 0.25F + random.nextFloat() * 0.07F;
            c.turn = random.nextFloat() * (float)(Math.PI / 3.0);
            Vec3 helper = Math.abs(c.dir.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            c.e1 = c.dir.cross(helper).normalize();
            c.e2 = c.dir.cross(c.e1).normalize();
            out[i] = c;
        }
        return out;
    }

    static void cube(PoseStack.Pose pose, VertexConsumer buffer, Vec3 centre, float half, Quaternionf spin, int frame) {
        float v0 = frame / 3.0F;
        float v1 = (frame + 1) / 3.0F;
        int[][] faces = {
            {0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}
        };
        Vec3[] corner = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            Vector3f v = new Vector3f((i & 1) == 0 ? -half : half, (i & 2) == 0 ? -half : half, (i & 4) == 0 ? -half : half);
            spin.transform(v);
            corner[i] = centre.add(v.x, v.y, v.z);
        }
        int[] map = {0, 1, 3, 2, 4, 5, 7, 6};
        for (int[] face : faces) {
            Frame.quad(buffer, pose, corner[map[face[0]]], corner[map[face[1]]], corner[map[face[2]]], corner[map[face[3]]], 0.0F, v0, 1.0F, v1, 0xFFFFFFFF);
        }
    }

    private static final class Rock {
        Vec3 from;
        Vec3 to;
        Vec3 dir;
        float launch;
        float fall;
        boolean big;
        float half;
        Vector3f spinAxis;
        float spinRate;
        Vec3[][] cracks;
        Crystal[] crystals;
        boolean burst;
        boolean shattered;

        float impact() {
            return this.launch + this.fall;
        }

        Vec3 at(float age) {
            double t = Mth.clamp((age - this.launch) / this.fall, 0.0F, 1.0F);
            return this.from.lerp(this.to, Math.pow(t, 1.15));
        }
    }

    private static final class Crystal {
        Vec3 base;
        Vec3 dir;
        Vec3 e1;
        Vec3 e2;
        float height;
        float radius;
        float turn;

        Vec3 point(Vec3 relBase, double y, double angle, double radius) {
            return relBase.add(this.dir.scale(y)).add(this.e1.scale(Math.cos(angle) * radius)).add(this.e2.scale(Math.sin(angle) * radius));
        }
    }

    private static final class Riser {
        Vec3 base;
        int rgb;
        float height;
        int life;
        int age;
    }

    private static final class Streak {
        Vec3 from;
        Vec3 dir;
        float start;
        float life;
        double length;
        double distance;
    }
}
