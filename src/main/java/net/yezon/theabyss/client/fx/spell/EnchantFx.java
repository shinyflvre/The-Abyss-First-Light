package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;

public final class EnchantFx {
    static final int MAGMA = 0x00D3C4;
    static final int MAGMA_HOT = 0x72FFF2;
    static final int MAGMA_SMOKE = 0x0F2E31;
    static final int WHITE = 0xEFFFFD;
    static final int ECHO = 0x9A52E0;
    static final int ECHO_PALE = 0xE0C8FF;
    static final int ECHO_DEEP = 0x55238F;
    static final int CRYSTAL = 0x5FF5F0;
    static final int CRYSTAL_DEEP = 0x2CC7D6;
    static final int DRAW = 0x30D8C8;
    static final int SEEK = 0xB896FF;
    static final int LIGHT = 0xD8FFFA;

    private EnchantFx() {
    }

    private static Vec3[] axes(Vec3 dir) {
        Vec3 s1 = dir.cross(new Vec3(0.0, 1.0, 0.0));
        s1 = s1.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : s1.normalize();
        Vec3 s2 = s1.cross(dir).normalize();
        return new Vec3[] {s1, s2};
    }

    private static Vec3 body(Frame f, int id, Vec3 fallback) {
        Entity e = f.level.getEntity(id);
        return e == null ? fallback : e.getPosition(f.pt).add(0.0, e.getBbHeight() * 0.55, 0.0);
    }

    public static DrawCharge drawCharge() {
        return new DrawCharge();
    }

    static final class Smelt extends Effect {
        private final Vec3 centre;
        private boolean burst;

        Smelt(SpellFxMessage m) {
            this.centre = SpellFx.vec(m, 0);
        }

        @Override
        public float life() {
            return 18.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = Math.max(0.0F, 1.0F - age / this.life());
            double e = easeOut(age / 9.0);
            double s = 0.5 * (1.0 - 0.8 * e);
            Vec3 c = this.centre.add(0.0, -0.35 * e, 0.0);
            Vec3[] corner = new Vec3[8];
            for (int i = 0; i < 8; i++) {
                corner[i] = c.add(((i & 1) == 0 ? -s : s), ((i & 2) == 0 ? -s : s), ((i & 4) == 0 ? -s : s));
            }
            int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
            for (int[] edge : edges) {
                f.line(corner[edge[0]], corner[edge[1]], 0.07F * k + 0.02F, k, MAGMA);
                f.line(corner[edge[0]], corner[edge[1]], 0.025F * k, k, WHITE);
            }
            f.flare(c, 3.2F * k + 0.6F, MAGMA, k);
            f.flare(c, 1.2F * k, WHITE, k * k);
            Vec3 floor = this.centre.add(0.0, -0.47, 0.0);
            f.ground(floor, 0.35 + 0.9 * e, 0.12F * k, k, MAGMA_HOT);
            f.ground(floor, 0.2 + 0.5 * e, 0.06F * k, k * 0.8F, WHITE);
            for (int i = 0; i < 4; i++) {
                double a = age * 0.35 + i * Math.PI * 0.5;
                Vec3 base = floor.add(Math.cos(a) * 0.45, 0.0, Math.sin(a) * 0.45);
                f.line(base, base.add(0.0, 0.6 + 0.6 * e, 0.0), 0.05F * k, k * 0.6F, MAGMA);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            for (int i = 0; i < 14; i++) {
                Vec3 v = new Vec3(random.nextGaussian() * 0.08, 0.12 + random.nextDouble() * 0.14, random.nextGaussian() * 0.08);
                emit(level, flame(i % 3 == 0 ? WHITE : MAGMA, 0.13F, 16 + random.nextInt(8)).gravity(0.035F).flags(FxOptions.ANIMATE | FxOptions.HOT | FxOptions.BOUNCE),
                    this.centre, v);
            }
            for (int i = 0; i < 4; i++) {
                emit(level, smoke(MAGMA_SMOKE, 0.45F, 34).gravity(-0.01F), this.centre.add(random.nextGaussian() * 0.2, 0.2, random.nextGaussian() * 0.2),
                    new Vec3(0.0, 0.02, 0.0));
            }
        }
    }

    static final class Echo extends Effect {
        private final int target;
        private final Vec3 from;
        private final Vec3 fallback;
        private final float delay;
        private final double tilt;
        private boolean struck;

        Echo(SpellFxMessage m) {
            this.target = m.ids().get(0);
            this.from = SpellFx.vec(m, 0);
            this.fallback = SpellFx.vec(m, 3);
            this.delay = m.ticks();
            this.tilt = (this.from.x * 7.0 + this.from.z * 3.0) % 1.0 > 0.5 ? 0.55 : -0.55;
        }

        @Override
        public float life() {
            return this.delay + 12.0F;
        }

        private Vec3[] crescent(Vec3 centre, Vec3 dir, double radius, double spread) {
            Vec3[] ax = axes(dir);
            Vec3 u = ax[0].scale(Math.cos(this.tilt)).add(ax[1].scale(Math.sin(this.tilt)));
            Vec3 v = ax[1].scale(Math.cos(this.tilt)).subtract(ax[0].scale(Math.sin(this.tilt)));
            int n = 16;
            Vec3[] p = new Vec3[n + 1];
            for (int i = 0; i <= n; i++) {
                double a = -spread * 0.5 + spread * i / n;
                p[i] = centre.add(u.scale(Math.cos(a) * radius)).add(v.scale(Math.sin(a) * radius)).subtract(dir.scale(Math.cos(a) * radius * 0.6));
            }
            return p;
        }

        private static float[] taper(int n, float max) {
            float[] w = new float[n];
            for (int i = 0; i < n; i++) {
                w[i] = (float)(max * Math.sin(Math.PI * i / (n - 1)));
            }
            return w;
        }

        private static float[] fill(int n, float value) {
            float[] l = new float[n];
            java.util.Arrays.fill(l, value);
            return l;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            Vec3 body = body(f, this.target, this.fallback);
            Vec3 dir = body.subtract(this.from);
            dir = new Vec3(dir.x, 0.0, dir.z);
            dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : dir.normalize();
            if (age < this.delay) {
                float grow = age / this.delay;
                Vec3 at = body.subtract(dir.scale(1.3));
                Vec3[] arc = this.crescent(at, dir, 1.1, Math.PI * 0.9 * grow + 0.3);
                f.ribbon(arc, taper(arc.length, 0.34F * grow), fill(arc.length, 0.45F * grow), ECHO);
                f.ribbon(arc, taper(arc.length, 0.08F), fill(arc.length, grow), ECHO_PALE);
                f.flare(at, 1.4F * grow, ECHO, grow);
                return;
            }
            float t = age - this.delay;
            float k = Math.max(0.0F, 1.0F - t / 12.0F);
            double sweep = easeOut(t / 3.0);
            for (int ghost = 2; ghost >= 0; ghost--) {
                double s = Math.max(0.0, sweep - ghost * 0.18);
                Vec3 at = body.subtract(dir.scale(1.3)).add(dir.scale(2.6 * s));
                Vec3[] arc = this.crescent(at, dir, 1.15, Math.PI * 0.95);
                float light = k * (ghost == 0 ? 1.0F : 0.35F / ghost);
                f.ribbon(arc, taper(arc.length, 0.42F), fill(arc.length, light * 0.7F), ECHO);
                f.ribbon(arc, taper(arc.length, 0.14F), fill(arc.length, light), ECHO_PALE);
            }
            float hit = t < 4.0F ? 1.0F - t / 4.0F : 0.0F;
            f.flare(body, 4.5F * hit + 1.0F * k, ECHO, k);
            f.flare(body, 1.6F * hit, 0xFFFFFF, hit);
            Vec3[] ax = axes(dir);
            f.circle(body, ax[0], ax[1], 0.4 + 1.4 * easeOut(t / 6.0), 0.12F * k, k, ECHO_DEEP);
            f.line(body.subtract(ax[1].scale(1.2)).subtract(ax[0].scale(0.9)), body.add(ax[1].scale(1.2)).add(ax[0].scale(0.9)), 0.1F * hit, hit, ECHO_PALE);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.struck || this.age() < this.delay) {
                return;
            }
            this.struck = true;
            Entity e = level.getEntity(this.target);
            Vec3 at = e == null ? this.fallback : e.position().add(0.0, e.getBbHeight() * 0.55, 0.0);
            RandomSource random = level.getRandom();
            for (int i = 0; i < 22; i++) {
                Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.6, random.nextGaussian()).normalize();
                emit(level, i % 3 == 0 ? sparkle(ECHO_PALE, 0.16F, 18) : wisp(ECHO, 0.24F, 20), at, d.scale(0.12 + random.nextDouble() * 0.2));
            }
        }
    }

    static final class Shatter extends Effect {
        private final Vec3 at;
        private final double radius;
        private final Vec3[] shards;
        private final double[] spin;
        private boolean burst;

        Shatter(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            this.radius = m.data().get(3);
            RandomSource random = RandomSource.create((long)(this.at.x * 31.0 + this.at.z * 17.0 + this.at.y));
            this.shards = new Vec3[18];
            this.spin = new double[18];
            for (int i = 0; i < this.shards.length; i++) {
                this.shards[i] = new Vec3(random.nextGaussian(), Math.abs(random.nextGaussian()) * 0.7 + 0.15, random.nextGaussian()).normalize();
                this.spin[i] = random.nextDouble() * Math.PI * 2.0;
            }
        }

        @Override
        public float life() {
            return 20.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = Math.max(0.0F, 1.0F - age / this.life());
            double e = easeOut(age / 11.0);
            for (int i = 0; i < this.shards.length; i++) {
                Vec3 d = this.shards[i];
                Vec3 mid = this.at.add(d.scale(0.3 + this.radius * 0.85 * e)).add(0.0, -0.6 * e * e, 0.0);
                Vec3[] ax = axes(d);
                double a = this.spin[i] + age * 0.5;
                Vec3 side = ax[0].scale(Math.cos(a)).add(ax[1].scale(Math.sin(a)));
                Vec3 tail = mid.subtract(d.scale(0.45 * k + 0.1));
                Vec3 tip = mid.add(d.scale(0.35));
                Vec3[] diamond = {tail, mid.add(side.scale(0.02)), tip};
                f.ribbon(diamond, new float[] {0.0F, 0.26F * k + 0.06F, 0.0F}, new float[] {0.4F * k, k, k}, CRYSTAL_DEEP);
                f.ribbon(diamond, new float[] {0.0F, 0.08F * k + 0.02F, 0.0F}, new float[] {k, k, k}, WHITE);
            }
            f.flare(this.at, 6.0F * k, CRYSTAL, k);
            f.flare(this.at, 2.4F * k, WHITE, k * k);
            Vec3 ground = this.at.add(0.0, -0.9, 0.0);
            f.ground(ground, this.radius * e, 0.3F * k, k, CRYSTAL);
            f.ground(ground, this.radius * 0.7 * easeOut(Math.max(0.0, age - 3.0) / 10.0), 0.14F * k, k * 0.8F, WHITE);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            for (int i = 0; i < 26; i++) {
                Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.5 + 0.3, random.nextGaussian()).normalize();
                emit(level, i % 2 == 0 ? sparkle(WHITE, 0.15F, 20) : orb(CRYSTAL, 0.12F, 18).gravity(0.03F).flags(FxOptions.SHRINK | FxOptions.BOUNCE),
                    this.at, d.scale(0.2 + random.nextDouble() * 0.3));
            }
        }
    }

    static final class Shot extends Effect {
        private final Vec3 at;
        private final Vec3 dir;

        Shot(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            Vec3 d = SpellFx.vec(m, 3);
            this.dir = d.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : d.normalize();
        }

        @Override
        public float life() {
            return 14.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = Math.max(0.0F, 1.0F - age / this.life());
            Vec3[] ax = axes(this.dir);
            double e = easeOut(age / 7.0);
            Vec3 c = this.at.add(this.dir.scale(0.8 + 1.6 * e));
            f.circle(c, ax[0], ax[1], 0.25 + 1.3 * e, 0.16F * k, k, DRAW);
            f.circle(this.at.add(this.dir.scale(0.6 + 3.2 * e)), ax[0], ax[1], 0.15 + 0.7 * e, 0.08F * k, k * 0.7F, WHITE);
            for (int i = 0; i < 3; i++) {
                double a = age * 0.4 + i * Math.PI * 2.0 / 3.0;
                Vec3 p = c.add(ax[0].scale(Math.cos(a) * (0.25 + 1.3 * e))).add(ax[1].scale(Math.sin(a) * (0.25 + 1.3 * e)));
                f.flare(p, 0.9F * k, DRAW, k);
            }
            f.line(this.at, this.at.add(this.dir.scale(4.0 + 6.0 * e)), 0.18F * k, k * 0.8F, DRAW);
            f.flare(this.at.add(this.dir.scale(0.8)), 3.0F * k, DRAW, k);
        }
    }

    static final class Hit extends Effect {
        private final Vec3 at;
        private final Vec3 dir;
        private final Vec3[] rays;
        private boolean burst;

        Hit(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            Vec3 d = SpellFx.vec(m, 3);
            this.dir = d.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : d.normalize();
            RandomSource random = RandomSource.create((long)(this.at.x * 11.0 + this.at.z * 5.0));
            this.rays = new Vec3[12];
            for (int i = 0; i < this.rays.length; i++) {
                this.rays[i] = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().add(this.dir.scale(0.6)).normalize();
            }
        }

        @Override
        public float life() {
            return 16.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = Math.max(0.0F, 1.0F - age / this.life());
            double e = easeOut(age / 6.0);
            for (Vec3 r : this.rays) {
                f.line(this.at.add(r.scale(0.3 + 1.8 * e)), this.at.add(r.scale(0.6 + 3.2 * e)), 0.1F * k, k, DRAW);
            }
            Vec3[] ax = axes(this.dir);
            f.circle(this.at, ax[0], ax[1], 0.3 + 2.2 * e, 0.22F * k, k, DRAW);
            f.circle(this.at, ax[0], ax[1], 0.2 + 1.2 * e, 0.1F * k, k, WHITE);
            f.flare(this.at, 7.0F * k, DRAW, k);
            f.flare(this.at, 2.6F * k, WHITE, k * k);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            for (int i = 0; i < 20; i++) {
                Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                emit(level, i % 2 == 0 ? sparkle(WHITE, 0.15F, 18) : zap(DRAW, 0.18F, 10), this.at, d.scale(0.25 + random.nextDouble() * 0.2));
            }
        }
    }

    static final class Lock extends Effect {
        private final int target;
        private final Vec3 fallback;

        Lock(SpellFxMessage m) {
            this.target = m.ids().get(0);
            this.fallback = SpellFx.vec(m, 0);
        }

        @Override
        public float life() {
            return 14.0F;
        }

        @Override
        public int outline(Entity entity) {
            return entity.getId() == this.target ? 0xFFB896FF : 0;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = this.fade(2.0F, 6.0F);
            Vec3 body = body(f, this.target, this.fallback);
            Vec3 view = f.camera.subtract(body);
            Vec3 dir = view.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : view.normalize();
            Vec3[] ax = axes(dir);
            double r = 1.6 - 0.7 * easeOut(age / 6.0);
            double turn = age * 0.25;
            f.circle(body, ax[0], ax[1], r, 0.06F, k, SEEK);
            for (int i = 0; i < 4; i++) {
                double a = turn + i * Math.PI * 0.5;
                Vec3 out = ax[0].scale(Math.cos(a)).add(ax[1].scale(Math.sin(a)));
                f.line(body.add(out.scale(r + 0.15)), body.add(out.scale(r + 0.55)), 0.09F, k, SEEK);
            }
            f.flare(body, 1.2F * k, SEEK, k);
        }
    }

    static final class Beam extends Effect {
        private final Vec3 at;
        private final Vec3 dir;

        Beam(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            Vec3 d = SpellFx.vec(m, 3);
            this.dir = d.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : d.normalize();
        }

        @Override
        public float life() {
            return 12.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = Math.max(0.0F, 1.0F - age / this.life());
            Vec3 a = this.at.subtract(this.dir.scale(2.5));
            Vec3 b = this.at.add(this.dir.scale(6.0 * easeOut(age / 3.0) + 1.0));
            f.line(a, b, 0.32F * k, k * 0.8F, LIGHT);
            f.line(a, b, 0.1F * k, k, 0xFFFFFF);
            Vec3[] ax = axes(this.dir);
            f.circle(this.at, ax[0], ax[1], 0.4 + 0.9 * easeOut(age / 5.0), 0.12F * k, k, LIGHT);
            f.flare(this.at, 4.0F * k, LIGHT, k);
        }
    }

    public static final class DrawCharge extends Effect {
        private float strength;
        private boolean active = true;

        @Override
        public float life() {
            return 100000.0F;
        }

        @Override
        public boolean done() {
            return !this.active && this.strength <= 0.0F || super.done();
        }

        public void set(boolean on) {
            this.active = on;
        }

        @Override
        public void tick(ClientLevel level) {
            this.strength = Math.max(0.0F, Math.min(1.0F, this.strength + (this.active ? 0.12F : -0.25F)));
        }

        @Override
        public void draw(Frame f) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || this.strength <= 0.0F) {
                return;
            }
            float age = this.age();
            float s = this.strength;
            Vec3 look = player.getViewVector(f.pt);
            Vec3 eye = player.getEyePosition(f.pt);
            Vec3[] ax = axes(look);
            Vec3 c = eye.add(look.scale(1.25)).subtract(ax[1].scale(0.1));
            double pulse = 0.5 + 0.5 * Math.sin(age * 0.5);
            double r = 0.3 + 0.04 * pulse;
            f.circle(c, ax[0], ax[1], r, 0.018F, s * 0.9F, DRAW);
            f.circle(c, ax[0], ax[1], r * 0.62, 0.012F, s * 0.7F, WHITE);
            Vec3[] tri = new Vec3[4];
            for (int i = 0; i <= 3; i++) {
                double a = age * 0.12 + i * Math.PI * 2.0 / 3.0;
                tri[i] = c.add(ax[0].scale(Math.cos(a) * r * 0.9)).add(ax[1].scale(Math.sin(a) * r * 0.9));
            }
            f.ribbon(tri, 0.014F, s * 0.8F, DRAW);
            for (int i = 0; i < 3; i++) {
                double a = -age * 0.2 + i * Math.PI * 2.0 / 3.0;
                double rr = r * (1.6 - 0.6 * ((age * 0.05 + i / 3.0) % 1.0));
                Vec3 p = c.add(ax[0].scale(Math.cos(a) * rr)).add(ax[1].scale(Math.sin(a) * rr));
                f.flare(p, 0.35F * s, DRAW, s);
                f.line(p, c, 0.008F, s * 0.4F, DRAW);
            }
            f.flare(c, (float)(0.6 + 0.3 * pulse) * s, WHITE, s);
        }
    }
}
