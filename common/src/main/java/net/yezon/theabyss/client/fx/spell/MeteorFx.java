package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.secondary.Meteor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class MeteorFx extends Effect {
    private static final RenderType ROCK = RenderType.entityCutout(ResourceLocation.withDefaultNamespace("textures/block/magma.png"));
    private static final int FIRE = 0xFF5A1E;
    private static final int EMBER = 0xFFB040;
    private static final int HOT = 0xFFE6B0;
    private static final float AFTER = 70.0F;

    private final Vec3 sky;
    private final Vec3 spot;
    private final int impact;
    private final Vec3[][] cracks;
    private boolean burst;

    MeteorFx(SpellFxMessage m) {
        this.sky = SpellFx.vec(m, 0);
        this.spot = SpellFx.vec(m, 3);
        this.impact = m.ticks();
        RandomSource random = RandomSource.create((long)(this.spot.x * 31 + this.spot.z * 17));
        this.cracks = new Vec3[9][];
        for (int i = 0; i < this.cracks.length; i++) {
            double a = i * Math.PI * 2.0 / this.cracks.length + random.nextDouble() * 0.5;
            double length = 2.5 + random.nextDouble() * 2.5;
            int n = 7;
            Vec3[] p = new Vec3[n + 1];
            for (int k = 0; k <= n; k++) {
                double r = 0.6 + length * k / n;
                double wobble = (random.nextDouble() - 0.5) * 0.45;
                p[k] = this.spot.add(Math.cos(a + wobble / r) * r, 0.06, Math.sin(a + wobble / r) * r);
            }
            this.cracks[i] = p;
        }
    }

    @Override
    public float life() {
        return this.impact + AFTER;
    }

    private double fall(float age) {
        if (age < Meteor.FALL_START) {
            return -1.0;
        }
        double f = Math.min(1.0, (age - Meteor.FALL_START) / (double)(this.impact - Meteor.FALL_START));
        return Math.pow(f, 1.7);
    }

    private Vec3 rock(float age) {
        return this.sky.lerp(this.spot, Math.max(0.0, this.fall(age)));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        Vec3 ground = this.spot.add(0.0, 0.07, 0.0);
        double r = Meteor.RADIUS;
        if (age < this.impact) {
            net.yezon.theabyss.client.fx.MagicLights.add(this.rock(age), FIRE, 12.0F, Math.min(1.0F, age / 6.0F));
        }
        if (age < this.impact + 3) {
            float in = Math.min(1.0F, age / 4.0F);
            double come = Math.max(0.0, this.fall(age));
            float heat = (float)(0.55 + 0.45 * come);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3.0 + age * 0.05;
                Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
                f.line(ground.add(dir.scale(r * 0.5)), ground.add(dir.scale(r * 0.95)), 0.18F, in * heat, FIRE);
            }
            float beam = (float)(0.25 + 0.75 * come) * in;
            f.line(this.sky, this.spot, 0.5F * beam, beam * 0.6F, FIRE);
            f.line(this.sky, this.spot, 0.12F * beam, beam * 0.8F, HOT);
        }
        double fall = this.fall(age);
        if (fall >= 0.0 && age < this.impact) {
            Vec3 at = this.rock(age);
            Vec3 dir = this.spot.subtract(this.sky).normalize();
            float grow = (float)Math.min(1.0, (age - Meteor.FALL_START) / 4.0);
            f.flare(at, 10.0F * grow, FIRE, 0.9F);
            f.flare(at, 4.5F * grow, HOT, 1.0F);
            double tail = 22.0 * Math.min(1.0, fall * 4.0 + 0.2);
            int n = 16;
            Vec3[] p = new Vec3[n];
            float[] w = new float[n];
            float[] l = new float[n];
            float[] wi = new float[n];
            for (int k = 0; k < n; k++) {
                double s = k / (double)(n - 1);
                p[k] = at.subtract(dir.scale(tail * s));
                w[k] = (float)(3.4 * (1.0 - s) + 0.2) * grow;
                wi[k] = (float)(1.3 * (1.0 - s) + 0.05) * grow;
                l[k] = (float)((1.0 - s) * (1.0 - s));
            }
            f.ribbon(p, w, l, FIRE);
            f.ribbon(p, wi, l, HOT);
            float tumble = age * 0.32F;
            Vec3 axis = dir.cross(new Vec3(0.0, 1.0, 0.0));
            Quaternionf spin = new Quaternionf().rotateAxis(tumble, (float)axis.x, (float)axis.y, (float)axis.z).rotateY(age * 0.11F);
            int frame = (int)(age / 3.0F) % 3;
            Vec3 rel = f.rel(at);
            f.mesh(ROCK, (pose, buffer, cam) -> cube(pose, buffer, rel, 1.1F * grow, spin, frame));
        }
        if (age >= this.impact) {
            float i = age - this.impact;
            if (i < 8.0F) {
                float k = 1.0F - i / 8.0F;
                f.flare(this.spot.add(0.0, 1.2, 0.0), 22.0F * (0.4F + 0.6F * k), HOT, k);
                f.flare(this.spot.add(0.0, 1.0, 0.0), 12.0F, 0xFFFFFF, k * k);
            }
            if (i < 12.0F) {
                float k = 1.0F - i / 12.0F;
                f.line(this.spot, this.spot.add(0.0, 14.0 * easeOut(i / 4.0), 0.0), 3.5F * k, k, FIRE);
                f.line(this.spot, this.spot.add(0.0, 10.0 * easeOut(i / 4.0), 0.0), 1.2F * k, k, HOT);
            }
            float glow = (float)Math.max(0.0, 1.0 - i / AFTER);
            float flicker = 0.75F + 0.25F * (float)Math.sin(age * 1.3);
            for (Vec3[] crack : this.cracks) {
                f.ribbon(crack, 0.16F, glow * flicker, FIRE);
                f.ribbon(crack, 0.06F, glow * flicker, HOT);
            }
        }
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        if (age < this.impact) {
            TrailStreaks.emit(this.rock(Math.max(0.0F, age - 1.0F)), this.rock(age), EMBER, 4);
            for (int k = 0; k < 4; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                Vec3 at = this.spot.add(Math.cos(a) * Meteor.RADIUS, 0.1, Math.sin(a) * Meteor.RADIUS);
                emit(level, flame(k % 2 == 0 ? FIRE : EMBER, 0.45F, 12).gravity(-0.03F), at, new Vec3(0.0, 0.04, 0.0));
            }
        }
        double fall = this.fall(age);
        if (fall >= 0.0 && age < this.impact) {
            Vec3 at = this.rock(age);
            Vec3 dir = this.spot.subtract(this.sky).normalize();
            for (int k = 0; k < 5; k++) {
                Vec3 off = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(0.35);
                emit(level, flame(k % 2 == 0 ? FIRE : EMBER, 0.6F + random.nextFloat() * 0.4F, 14).drag(0.88F), at.add(off),
                    dir.scale(-0.15).add(off.scale(0.05)));
            }
            emit(level, smoke(0x24160F, 1.4F + random.nextFloat(), 46).gravity(-0.004F).drag(0.92F), at.subtract(dir.scale(1.5)), dir.scale(-0.05));
            emit(level, sparkle(EMBER, 0.14F, 26).gravity(0.03F).flags(FxOptions.FLICKER | FxOptions.HOT), at,
                new Vec3(random.nextGaussian() * 0.12, random.nextGaussian() * 0.12, random.nextGaussian() * 0.12));
        }
        if (age >= this.impact && !this.burst) {
            this.burst = true;
            Vec3 c = this.spot.add(0.0, 0.3, 0.0);
            for (int k = 0; k < 70; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double s = 0.25 + random.nextDouble() * 0.55;
                emit(level, sparkle(k % 3 == 0 ? HOT : EMBER, 0.14F, 40).gravity(0.05F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE), c,
                    new Vec3(Math.cos(a) * s, 0.35 + random.nextDouble() * 0.6, Math.sin(a) * s));
            }
            for (int k = 0; k < 48; k++) {
                double a = k * Math.PI * 2.0 / 48;
                emit(level, flame(k % 2 == 0 ? FIRE : EMBER, 0.8F, 18).drag(0.9F), c.add(Math.cos(a) * 0.8, 0.0, Math.sin(a) * 0.8),
                    new Vec3(Math.cos(a) * 0.9, 0.05, Math.sin(a) * 0.9));
            }
            for (int k = 0; k < 22; k++) {
                emit(level, smoke(k % 2 == 0 ? 0x2A1C16 : 0x4A3A30, 2.0F + random.nextFloat() * 1.5F, 70).gravity(-0.01F).drag(0.93F),
                    c.add(random.nextGaussian() * 1.2, random.nextDouble() * 1.5, random.nextGaussian() * 1.2),
                    new Vec3(random.nextGaussian() * 0.08, 0.12 + random.nextDouble() * 0.1, random.nextGaussian() * 0.08));
            }
        }
        if (age >= this.impact && age < this.impact + AFTER - 10) {
            for (int k = 0; k < 2; k++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = Math.sqrt(random.nextDouble()) * 3.0;
                Vec3 at = this.spot.add(Math.cos(a) * d, 0.1, Math.sin(a) * d);
                emit(level, flame(k == 0 ? FIRE : EMBER, 0.45F, 14).gravity(-0.02F), at, new Vec3(0.0, 0.04, 0.0));
            }
            if (random.nextInt(3) == 0) {
                emit(level, smoke(0x3A2C26, 1.2F, 50).gravity(-0.008F), this.spot.add(random.nextGaussian(), 0.6, random.nextGaussian()),
                    new Vec3(0.0, 0.05, 0.0));
            }
        }
    }

    private static void cube(com.mojang.blaze3d.vertex.PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer buffer, Vec3 centre, float half,
                             Quaternionf spin, int frame) {
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
}
