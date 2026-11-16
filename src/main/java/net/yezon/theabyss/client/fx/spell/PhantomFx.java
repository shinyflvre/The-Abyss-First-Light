package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.yezon.theabyss.entity.InfectedPhantomEntity;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;

final class PhantomFx {
    static final int TOXIC = 0x3DFFC8;
    static final int VENOM = 0x2EF0E6;
    static final int CYAN = 0x52F5FF;
    static final int PALE = 0xDDFFFB;
    static final int GAS = 0x2A9E8C;
    static final int MOSS = 0x3FC9AE;

    private PhantomFx() {
    }

    static Effect splash(SpellFxMessage m) {
        int id = m.ids().get(0);
        Vec3 at = SpellFx.vec(m, 0);
        for (Effect effect : SpellFx.effects()) {
            if (effect instanceof Bolt bolt && bolt.id == id) {
                bolt.stop(at);
            }
        }
        return new Splash(at, m.ticks() == 1);
    }

    private static InfectedPhantomEntity phantom(ClientLevel level, int id) {
        return level.getEntity(id) instanceof InfectedPhantomEntity p && p.isAlive() ? p : null;
    }

    private static Vec3 ground(ClientLevel level, Vec3 feet) {
        HitResult down = level.clip(new ClipContext(feet, feet.add(0.0, -8.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return (down.getType() == HitResult.Type.MISS ? feet : down.getLocation()).add(0.0, 0.06, 0.0);
    }

    private static Vec3[] axes(Vec3 dir) {
        Vec3 s1 = dir.cross(new Vec3(0.0, 1.0, 0.0));
        s1 = s1.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : s1.normalize();
        Vec3 s2 = dir.cross(s1).normalize();
        return new Vec3[] {s1, s2};
    }

    private static FxOptions drop(int colour, float size, int life) {
        return orb(colour, size, life).gravity(0.045F).flags(FxOptions.SHRINK | FxOptions.BOUNCE);
    }

    private static FxOptions orb(int colour, float size, int life) {
        return Effect.orb(colour, size, life);
    }

    static final class Charge extends Effect {
        private final int phantom;
        private final int ticks;
        private Vec3 hand;

        Charge(SpellFxMessage m) {
            this.phantom = m.ids().get(0);
            this.ticks = m.ticks();
        }

        @Override
        public float life() {
            return this.ticks + 1.0F;
        }

        private Vec3 hand(InfectedPhantomEntity p, float pt) {
            return InfectedPhantomEntity.hand(p.getPosition(pt), Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot));
        }

        @Override
        public void draw(Frame f) {
            InfectedPhantomEntity p = phantom(f.level, this.phantom);
            if (p == null || this.age() > 2.0F && p.attack() != InfectedPhantomEntity.VENOM) {
                this.kill();
                return;
            }
            float age = this.age();
            float k = Mth.clamp(age / this.ticks, 0.0F, 1.0F);
            Vec3 hand = this.hand(p, f.pt);
            float pulse = 0.85F + 0.15F * Mth.sin(age * 1.7F);
            f.flare(hand, (0.4F + 1.5F * k) * pulse, VENOM, 0.5F + 0.5F * k);
            f.flare(hand, 0.15F + 0.55F * k, PALE, k);
            for (int i = 0; i < 3; i++) {
                int n = 9;
                Vec3[] pts = new Vec3[n];
                float[] w = new float[n];
                float[] l = new float[n];
                for (int j = 0; j < n; j++) {
                    float s = j / (float)(n - 1);
                    double a = i * Math.PI * 2.0 / 3.0 + age * 0.33 + s * 2.4;
                    double r = (1.4 - 1.0 * k) * (1.0 - s) + 0.04;
                    double y = (1.0 - s) * 0.5 * Math.sin(i * 2.1 + age * 0.2);
                    pts[j] = hand.add(Math.cos(a) * r, y, Math.sin(a) * r);
                    w[j] = 0.03F + 0.05F * s;
                    l[j] = s * (0.3F + 0.7F * k);
                }
                f.ribbon(pts, w, l, i == 1 ? CYAN : TOXIC);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            InfectedPhantomEntity p = phantom(level, this.phantom);
            if (p == null) {
                return;
            }
            RandomSource random = level.getRandom();
            Vec3 hand = this.hand(p, 1.0F);
            for (int i = 0; i < 2; i++) {
                Vec3 off = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(0.8 + random.nextDouble() * 0.5);
                emit(level, wisp(random.nextBoolean() ? TOXIC : VENOM, 0.12F, 9), hand.add(off), off.scale(-0.1));
            }
            if (random.nextInt(2) == 0) {
                emit(level, drop(TOXIC, 0.06F, 18), hand.add(0.0, -0.1, 0.0), new Vec3(0.0, -0.02, 0.0));
            }
        }
    }

    static final class Bolt extends Effect {
        private static final float TAIL = 3.4F;
        final int id;
        private final Vec3 from;
        private final Vec3 velocity;
        private final Vec3[] axes;
        private final int ticks;
        private float stopAge = -1.0F;
        private Vec3 stopAt;

        Bolt(SpellFxMessage m) {
            this.id = m.ids().get(0);
            this.from = SpellFx.vec(m, 0);
            this.velocity = SpellFx.vec(m, 3);
            this.axes = axes(this.velocity.normalize());
            this.ticks = m.ticks();
        }

        void stop(Vec3 at) {
            if (this.stopAge < 0.0F) {
                this.stopAge = Math.min(this.age(), this.ticks);
                this.stopAt = at;
            }
        }

        @Override
        public float life() {
            return (this.stopAge < 0.0F ? this.ticks : this.stopAge) + 4.0F;
        }

        private Vec3 at(float t) {
            if (this.stopAge >= 0.0F && t >= this.stopAge) {
                return this.stopAt;
            }
            return this.from.add(this.velocity.scale(Math.min(t, this.ticks)));
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float speed = (float)this.velocity.length();
            float tailTicks = TAIL / speed;
            float headT = this.stopAge >= 0.0F ? Math.min(age, this.stopAge) : Math.min(age, this.ticks);
            float tailT = Math.max(0.0F, age - tailTicks);
            if (this.stopAge >= 0.0F) {
                tailT = Math.max(tailT, this.stopAge - tailTicks + (age - this.stopAge) * tailTicks / 4.0F);
            }
            if (tailT >= headT) {
                return;
            }
            int n = 12;
            Vec3[] p = new Vec3[n];
            Vec3[] h1 = new Vec3[n];
            Vec3[] h2 = new Vec3[n];
            float[] w = new float[n];
            float[] wc = new float[n];
            float[] wh = new float[n];
            float[] l = new float[n];
            for (int i = 0; i < n; i++) {
                float s = i / (float)(n - 1);
                float t = Mth.lerp(s, headT, tailT);
                p[i] = this.at(t);
                double a = t * 1.1;
                double r = 0.2 * (1.0 - s * 0.6);
                Vec3 o = this.axes[0].scale(Math.cos(a) * r).add(this.axes[1].scale(Math.sin(a) * r));
                h1[i] = p[i].add(o);
                h2[i] = p[i].subtract(o);
                w[i] = 0.46F * (1.0F - s) + 0.03F;
                wc[i] = 0.16F * (1.0F - s) + 0.02F;
                wh[i] = 0.05F * (1.0F - s) + 0.01F;
                l[i] = (1.0F - s) * (1.0F - s * 0.3F);
            }
            f.ribbon(p, w, l, TOXIC);
            f.ribbon(p, wc, l, CYAN);
            f.ribbon(h1, wh, l, VENOM);
            f.ribbon(h2, wh, l, PALE);
            if (this.stopAge < 0.0F) {
                Vec3 head = p[0];
                float pulse = 0.9F + 0.1F * Mth.sin(age * 2.0F);
                f.flare(head, 1.4F * pulse, VENOM, 0.95F);
                f.flare(head, 0.5F, PALE, 1.0F);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.stopAge >= 0.0F) {
                return;
            }
            RandomSource random = level.getRandom();
            Vec3 head = this.at(this.age());
            emit(level, drop(random.nextBoolean() ? TOXIC : VENOM, 0.07F, 16), head.add(random.nextGaussian() * 0.1, -0.05, random.nextGaussian() * 0.1),
                this.velocity.scale(0.05));
            if (random.nextInt(3) == 0) {
                emit(level, sparkle(PALE, 0.08F, 8), head.add(random.nextGaussian() * 0.25, random.nextGaussian() * 0.25, random.nextGaussian() * 0.25), Vec3.ZERO);
            }
        }
    }

    static final class Splash extends Effect {
        private final Vec3 at;
        private final boolean fizzle;
        private final Vec3[] shards;
        private boolean burst;

        Splash(Vec3 at, boolean fizzle) {
            this.at = at;
            this.fizzle = fizzle;
            RandomSource random = RandomSource.create((long)(at.x * 31.0 + at.y * 17.0 + at.z * 7.0));
            this.shards = new Vec3[10];
            for (int i = 0; i < this.shards.length; i++) {
                this.shards[i] = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.5 + 0.35, random.nextGaussian()).normalize();
            }
        }

        @Override
        public float life() {
            return this.fizzle ? 10.0F : 20.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = 1.0F - age / this.life();
            float size = this.fizzle ? 0.5F : 1.0F;
            double e = easeOut(age / 8.0);
            f.flare(this.at, (3.2F * k + 0.3F) * size, VENOM, k);
            f.flare(this.at, 1.2F * k * size, PALE, k * k);
            if (this.fizzle) {
                return;
            }
            f.circle(this.at, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), 0.3 + 1.7 * e, 0.2F * k + 0.02F, k, TOXIC);
            for (Vec3 d : this.shards) {
                Vec3 a = this.at.add(d.scale(0.2 + 1.6 * e));
                Vec3 b = this.at.add(d.scale(0.2 + 1.6 * e + 0.55 * k));
                f.line(a, b, 0.09F * k + 0.02F, k, CYAN);
            }
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            int drops = this.fizzle ? 5 : 16;
            for (int i = 0; i < drops; i++) {
                Vec3 v = new Vec3(random.nextGaussian() * 0.14, 0.08 + random.nextDouble() * 0.18, random.nextGaussian() * 0.14);
                emit(level, drop(random.nextBoolean() ? TOXIC : VENOM, 0.08F + random.nextFloat() * 0.05F, 22 + random.nextInt(10)), this.at, v);
            }
            if (!this.fizzle) {
                for (int i = 0; i < 6; i++) {
                    Vec3 v = new Vec3(random.nextGaussian() * 0.04, 0.02 + random.nextDouble() * 0.03, random.nextGaussian() * 0.04);
                    emit(level, wisp(i % 2 == 0 ? MOSS : VENOM, 0.35F + random.nextFloat() * 0.2F, 20 + random.nextInt(10)).gravity(-0.002F), this.at, v);
                }
            }
        }
    }

    static final class Gather extends Effect {
        private final int phantom;
        private final double radius;
        private final int ticks;
        private Vec3 ground;

        Gather(SpellFxMessage m) {
            this.phantom = m.ids().get(0);
            this.radius = m.data().get(0);
            this.ticks = m.ticks();
        }

        @Override
        public float life() {
            return this.ticks + 1.0F;
        }

        @Override
        public void draw(Frame f) {
            InfectedPhantomEntity p = phantom(f.level, this.phantom);
            if (p == null || this.age() > 2.0F && p.attack() != InfectedPhantomEntity.MIASMA) {
                this.kill();
                return;
            }
            float age = this.age();
            float k = Mth.clamp(age / this.ticks, 0.0F, 1.0F);
            Vec3 feet = p.getPosition(f.pt);
            Vec3 g = this.ground != null ? new Vec3(feet.x, this.ground.y, feet.z) : ground(f.level, feet);
            float pulse = 0.7F + 0.3F * Mth.sin(age * 0.9F);
            f.ground(g, this.radius, 0.09F, (0.35F + 0.45F * k) * pulse, VENOM);
            f.ground(g, this.radius * smooth(k), 0.26F, 0.4F + 0.6F * k, TOXIC);
            f.ground(g, this.radius * smooth(k) * 0.96, 0.08F, k, PALE);
            double turn = age * 0.05;
            for (int i = 0; i < 7; i++) {
                double a = turn + i * Math.PI * 2.0 / 7.0;
                Vec3 rim = g.add(Math.cos(a) * this.radius, 0.0, Math.sin(a) * this.radius);
                Vec3 mid = g.add(Math.cos(a + 0.5) * this.radius * 0.5, 0.0, Math.sin(a + 0.5) * this.radius * 0.5);
                f.ribbon(Frame.curve(rim, mid, g, 10), 0.06F, 0.25F + 0.5F * k, CYAN);
            }
            Vec3 body = feet.add(0.0, p.getBbHeight() * 0.55, 0.0);
            for (int i = 0; i < 3; i++) {
                int n = 16;
                Vec3[] pts = new Vec3[n];
                for (int j = 0; j < n; j++) {
                    float s = j / (float)(n - 1);
                    double a = i * Math.PI * 2.0 / 3.0 + age * 0.45 + s * Math.PI * 2.5;
                    double r = (1.2 - 0.5 * k) * (1.0 - s * 0.5);
                    pts[j] = new Vec3(feet.x + Math.cos(a) * r, Mth.lerp(s, g.y, feet.y + p.getBbHeight() + 0.3), feet.z + Math.sin(a) * r);
                }
                f.ribbon(pts, 0.07F, 0.35F + 0.5F * k, i == 0 ? CYAN : TOXIC);
            }
            f.flare(body, (0.6F + 2.2F * k) * pulse, VENOM, 0.4F + 0.6F * k);
            f.flare(body, 0.3F + 0.8F * k, PALE, k);
        }

        @Override
        public void tick(ClientLevel level) {
            InfectedPhantomEntity p = phantom(level, this.phantom);
            if (p == null) {
                return;
            }
            Vec3 feet = p.position();
            if (this.ground == null) {
                this.ground = ground(level, feet);
            }
            RandomSource random = level.getRandom();
            Vec3 g = new Vec3(feet.x, this.ground.y, feet.z);
            for (int i = 0; i < 4; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = this.radius * (0.85 + random.nextDouble() * 0.2);
                Vec3 at = g.add(Math.cos(a) * r, 0.1 + random.nextDouble() * 0.4, Math.sin(a) * r);
                Vec3 v = g.subtract(at).scale(0.055).add(0.0, 0.015, 0.0);
                emit(level, wisp(random.nextBoolean() ? TOXIC : CYAN, 0.16F + random.nextFloat() * 0.08F, 16), at, v);
            }
            if (random.nextInt(2) == 0) {
                double a = random.nextDouble() * Math.PI * 2.0;
                Vec3 at = g.add(Math.cos(a) * this.radius * 0.9, 0.2, Math.sin(a) * this.radius * 0.9);
                emit(level, wisp(MOSS, 0.5F, 22).gravity(-0.001F), at, g.subtract(at).scale(0.03));
            }
        }
    }

    static final class Miasma extends Effect {
        private static final float WAVE = 8.0F;
        private final Vec3 centre;
        private final Vec3 ground;
        private final double radius;
        private final int cloud;
        private boolean burst;

        Miasma(SpellFxMessage m) {
            this.centre = SpellFx.vec(m, 0);
            this.ground = SpellFx.vec(m, 3).add(0.0, 0.06, 0.0);
            this.radius = m.data().get(6);
            this.cloud = m.ticks();
        }

        @Override
        public float life() {
            return this.cloud + 10.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            if (age < WAVE + 4.0F) {
                float k = Mth.clamp(1.0F - age / (WAVE + 4.0F), 0.0F, 1.0F);
                double s = easeOut(age / WAVE);
                f.ground(this.ground, this.radius * 1.1 * s, 0.55F * k + 0.05F, k, TOXIC);
                f.ground(this.ground, this.radius * 0.88 * s, 0.16F * k + 0.03F, k, CYAN);
                f.ground(this.ground, this.radius * 0.6 * s, 0.08F * k, k * 0.8F, PALE);
                double r = 0.5 + this.radius * 0.95 * s;
                float shell = k * 0.75F;
                for (int i = 0; i < 3; i++) {
                    double yaw = i * Math.PI / 3.0 + 0.3;
                    f.circle(this.centre, new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw)), new Vec3(0.0, 1.0, 0.0), r, 0.12F * k + 0.02F, shell, VENOM);
                }
                f.circle(this.centre, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), r, 0.16F * k + 0.02F, shell, CYAN);
            }
            if (age < 7.0F) {
                float k = 1.0F - age / 7.0F;
                f.flare(this.centre, 6.5F * k, VENOM, k);
                f.flare(this.centre, 2.6F * k, PALE, k * k);
            }
            float fade = this.fade(6.0F, 18.0F);
            float pulse = 0.75F + 0.25F * Mth.sin(age * 0.25F);
            f.ground(this.ground, this.radius * 0.8, 0.07F, 0.3F * fade * pulse, MOSS);
        }

        @Override
        public void tick(ClientLevel level) {
            RandomSource random = level.getRandom();
            if (!this.burst) {
                this.burst = true;
                for (int i = 0; i < 26; i++) {
                    Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.5 + 0.4, random.nextGaussian()).normalize();
                    emit(level, drop(random.nextBoolean() ? TOXIC : VENOM, 0.09F + random.nextFloat() * 0.05F, 24 + random.nextInt(12)), this.centre,
                        d.scale(0.25 + random.nextDouble() * 0.2));
                }
                for (int i = 0; i < 34; i++) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double r = this.radius * (1.0 - random.nextDouble() * random.nextDouble());
                    Vec3 at = this.ground.add(Math.cos(a) * r * 0.3, 0.3 + random.nextDouble() * 0.8, Math.sin(a) * r * 0.3);
                    Vec3 v = new Vec3(Math.cos(a) * r * 0.06, 0.01, Math.sin(a) * r * 0.06);
                    emit(level, wisp(i % 3 == 0 ? TOXIC : i % 3 == 1 ? MOSS : VENOM, 0.4F + random.nextFloat() * 0.35F, 26 + random.nextInt(14)).gravity(-0.001F)
                        .drag(0.9F), at, v);
                }
                for (int i = 0; i < 10; i++) {
                    emit(level, sparkle(PALE, 0.12F, 12), this.centre.add(random.nextGaussian() * 1.2, random.nextGaussian() * 0.8, random.nextGaussian() * 1.2),
                        Vec3.ZERO);
                }
            }
            if (this.age() > this.cloud) {
                return;
            }
            for (int i = 0; i < 2; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = this.radius * 0.8 * Math.sqrt(random.nextDouble());
                Vec3 at = this.ground.add(Math.cos(a) * r, 0.15 + random.nextDouble() * 0.9, Math.sin(a) * r);
                emit(level, wisp(random.nextInt(3) == 0 ? TOXIC : MOSS, 0.45F + random.nextFloat() * 0.4F, 30 + random.nextInt(14)).gravity(-0.0015F), at,
                    new Vec3(random.nextGaussian() * 0.008, 0.004, random.nextGaussian() * 0.008));
            }
            if (random.nextInt(3) == 0) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = this.radius * 0.75 * Math.sqrt(random.nextDouble());
                emit(level, orb(PALE, 0.06F, 26).gravity(-0.012F), this.ground.add(Math.cos(a) * r, 0.05, Math.sin(a) * r), Vec3.ZERO);
            }
        }
    }

    static final class Rend extends Effect {
        private final int phantom;
        private final int target;
        private final Vec3 origin;
        private final Vec3 rift;
        private final Vec3[] streaks;
        private final int ticks;
        private final Deque<Vec3> trail = new ArrayDeque<>();
        private float trailEnd = -1.0F;
        private Vec3 riftGround;

        Rend(SpellFxMessage m) {
            this.phantom = m.ids().get(0);
            this.target = m.ids().get(1);
            this.origin = SpellFx.vec(m, 0);
            this.rift = SpellFx.vec(m, 3).add(0.0, 1.0, 0.0);
            this.ticks = m.ticks();
            RandomSource random = RandomSource.create((long)(this.origin.x * 13.0 + this.origin.z * 29.0));
            this.streaks = new Vec3[9];
            for (int i = 0; i < this.streaks.length; i++) {
                this.streaks[i] = new Vec3(random.nextGaussian() * 0.3, random.nextGaussian() * 0.6, random.nextGaussian() * 0.3);
            }
        }

        @Override
        public float life() {
            return this.ticks + 10.0F;
        }

        private Vec3 side(Frame f) {
            Vec3 t = f.body(this.target);
            Vec3 to = t == null ? new Vec3(1.0, 0.0, 0.0) : new Vec3(t.x - this.rift.x, 0.0, t.z - this.rift.z);
            to = to.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : to.normalize();
            return new Vec3(-to.z, 0.0, to.x);
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            if (age < 10.0F) {
                float k = 1.0F - age / 10.0F;
                double rise = easeOut(age / 9.0) * 2.2;
                for (Vec3 s : this.streaks) {
                    Vec3 a = this.origin.add(s).add(0.0, rise, 0.0);
                    f.line(a, a.add(0.0, 0.7 * k + 0.1, 0.0), 0.06F * k + 0.01F, k, s.y > 0 ? TOXIC : CYAN);
                }
                if (age < 6.0F) {
                    float q = 1.0F - age / 6.0F;
                    f.flare(this.origin, 2.6F * q, VENOM, q);
                }
            }
            float lunge = InfectedPhantomEntity.REND_LUNGE;
            float open = (float)(smooth((age - 3.0F) / 9.0F) * (1.0F - smooth((age - lunge) / 5.0F)));
            if (open > 0.01F) {
                Vec3 side = this.side(f);
                Vec3 up = new Vec3(0.0, 1.0, 0.0);
                double h = 1.35;
                double w = 0.85 * open;
                float flicker = 0.85F + 0.15F * Mth.sin(age * 3.1F);
                Vec3 top = this.rift.add(up.scale(h));
                Vec3 bottom = this.rift.subtract(up.scale(h));
                Vec3[] left = Frame.curve(top, this.rift.add(side.scale(w * 1.9)), bottom, 14);
                Vec3[] right = Frame.curve(top, this.rift.subtract(side.scale(w * 1.9)), bottom, 14);
                f.ribbon(left, 0.2F, open * flicker, TOXIC);
                f.ribbon(right, 0.2F, open * flicker, TOXIC);
                f.ribbon(left, 0.07F, open, PALE);
                f.ribbon(right, 0.07F, open, PALE);
                f.flare(this.rift, (1.2F + 1.4F * open) * flicker, VENOM, 0.75F * open);
                f.line(top.add(0.0, 0.1, 0.0), bottom.subtract(0.0, 0.1, 0.0), 0.05F * open, open * 0.8F, CYAN);
                if (f.level.getRandom().nextInt(3) == 0) {
                    Vec3 from = this.rift.add(side.scale((f.level.getRandom().nextBoolean() ? 1 : -1) * w * 0.9)).add(0.0, (f.level.getRandom().nextDouble() - 0.5) * h, 0.0);
                    Vec3 dir = new Vec3(f.level.getRandom().nextGaussian(), f.level.getRandom().nextGaussian() * 0.6, f.level.getRandom().nextGaussian()).normalize();
                    f.zap(from, from.add(dir.scale(0.9)), 0.2, 0.05F, open, CYAN, f.level.getRandom());
                }
                Vec3 g = this.riftGround;
                if (g != null && this.rift.y - g.y < 4.0) {
                    f.ground(g, 0.6 + 0.6 * open, 0.08F, open * 0.6F, VENOM);
                }
            }
            if (this.trail.size() >= 2) {
                float k = this.trailEnd < 0.0F ? 1.0F : Math.max(0.0F, 1.0F - (age - this.trailEnd) / 8.0F);
                if (k > 0.0F) {
                    List<Vec3> list = new ArrayList<>();
                    Vec3 head = this.trailEnd < 0.0F ? f.body(this.phantom) : null;
                    if (head != null) {
                        list.add(head);
                    }
                    list.addAll(this.trail);
                    int n = list.size();
                    Vec3[] p = list.toArray(new Vec3[0]);
                    float[] w = new float[n];
                    float[] wc = new float[n];
                    float[] l = new float[n];
                    for (int i = 0; i < n; i++) {
                        float s = i / (float)(n - 1);
                        w[i] = 1.1F * (1.0F - s) + 0.05F;
                        wc[i] = 0.3F * (1.0F - s) + 0.02F;
                        l[i] = (1.0F - s) * k;
                    }
                    f.ribbon(p, w, l, VENOM);
                    f.ribbon(p, wc, l, PALE);
                }
            }
        }

        @Override
        public void tick(ClientLevel level) {
            float age = this.age();
            RandomSource random = level.getRandom();
            float lunge = InfectedPhantomEntity.REND_LUNGE;
            if (this.riftGround == null) {
                this.riftGround = ground(level, this.rift.subtract(0.0, 1.0, 0.0));
            }
            if (age < 8.0F) {
                for (int i = 0; i < 3; i++) {
                    emit(level, wisp(random.nextBoolean() ? TOXIC : CYAN, 0.18F, 14).gravity(-0.01F),
                        this.origin.add(random.nextGaussian() * 0.35, random.nextGaussian() * 0.6, random.nextGaussian() * 0.35), new Vec3(0.0, 0.06, 0.0));
                }
            }
            if (age > 3.0F && age < lunge) {
                for (int i = 0; i < 2; i++) {
                    Vec3 off = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.8, random.nextGaussian()).normalize().scale(1.6);
                    emit(level, wisp(random.nextBoolean() ? VENOM : TOXIC, 0.13F, 10), this.rift.add(off), off.scale(-0.12));
                }
            }
            Entity p = level.getEntity(this.phantom);
            boolean lunging = age >= lunge - 1.0F && age <= this.ticks + 1.0F;
            if (p != null && p.isAlive() && lunging) {
                this.trail.addFirst(p.position().add(0.0, p.getBbHeight() * 0.5, 0.0));
                while (this.trail.size() > 9) {
                    this.trail.removeLast();
                }
                emit(level, wisp(random.nextBoolean() ? TOXIC : VENOM, 0.22F, 14), p.position().add(random.nextGaussian() * 0.3, 0.5 + random.nextDouble(),
                    random.nextGaussian() * 0.3), Vec3.ZERO);
            } else if (!this.trail.isEmpty() && this.trailEnd < 0.0F && age > lunge) {
                this.trailEnd = age;
            }
        }
    }
}
