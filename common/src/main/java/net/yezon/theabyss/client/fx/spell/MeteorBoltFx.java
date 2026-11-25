package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.MeteorShower;
import net.yezon.theabyss.ring.fx.FxOptions;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class MeteorBoltFx extends Effect {
    private static final float AFTER = 22.0F;
    private static final float HALF = 0.15F;
    private static final double NEAR = 2.2;

    private final Vec3 from;
    private final Vec3 to;
    private final Vec3 dir;
    private final float flight;
    private final Vector3f spinAxis;
    private final float spinRate;
    private boolean burst;

    MeteorBoltFx(SpellFxMessage m) {
        this.from = SpellFx.vec(m, 0);
        this.to = SpellFx.vec(m, 3);
        this.flight = Math.max(1.0F, m.ticks());
        Vec3 path = this.to.subtract(this.from);
        this.dir = path.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : path.normalize();
        RandomSource random = RandomSource.create((long)(this.from.x * 13.0 + this.to.z * 7.0 + this.to.y));
        Vector3f axis = new Vector3f(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F);
        this.spinAxis = axis.lengthSquared() < 1.0E-4F ? new Vector3f(0.0F, 1.0F, 0.0F) : axis.normalize();
        this.spinRate = 0.5F + random.nextFloat() * 0.3F;
    }

    @Override
    public float life() {
        return this.flight + AFTER;
    }

    private Vec3 at(float age) {
        return this.from.lerp(this.to, Mth.clamp(age / this.flight, 0.0F, 1.0F));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        if (age < 3.0F) {
            float k = 1.0F - age / 3.0F;
            Vec3 muzzle = f.rel(this.from);
            f.add(MeteorShowerFx.HEAD[(int)age % MeteorShowerFx.HEAD.length], (pose, buffer) -> LightTrail.flare(pose, buffer, muzzle, 0.55F * k, 0xFFFFFF, 0.8F, Vec3.ZERO));
        }
        if (age < this.flight) {
            Vec3 at = this.at(age);
            double length = Math.min(at.distanceTo(this.from) + 0.3, 4.5);
            int n = 10;
            Vec3[] p = new Vec3[n];
            float[] w = new float[n];
            float[] l = new float[n];
            for (int j = 0; j < n; j++) {
                double s = j / (double)(n - 1);
                p[j] = at.subtract(this.dir.scale(length * s));
                w[j] = (float)(0.75 * Math.pow(1.0 - s, 0.8) + 0.06);
                l[j] = Mth.clamp((float)(1.3 - s), 0.0F, 1.0F);
            }
            f.ribbon(MeteorShowerFx.TRAIL[(int)(age / 2.0F) % MeteorShowerFx.TRAIL.length], p, w, l, 0xFFFFFF);
            int nb = 5;
            Vec3[] pb = new Vec3[nb];
            float[] wb = new float[nb];
            float[] lb = new float[nb];
            for (int j = 0; j < nb; j++) {
                double s = 0.4 * j / (nb - 1);
                pb[j] = at.subtract(this.dir.scale(length * s));
                wb[j] = (float)(0.25 * Math.pow(1.0 - s, 0.8) + 0.02);
                lb[j] = (float)Math.pow(Math.max(0.0, 1.0 - s / 0.4), 1.2);
            }
            f.ribbon(LightTrail.STREAK, pb, wb, lb, MeteorShower.PALE);
            Quaternionf spin = new Quaternionf().rotateAxis(age * this.spinRate, this.spinAxis.x, this.spinAxis.y, this.spinAxis.z);
            int frame = (int)(age / 3.0F) % 3;
            Vec3 rel = f.rel(at);
            f.mesh(MeteorShowerFx.ROCK, (pose, buffer, cam) -> MeteorShowerFx.cube(pose, buffer, rel, HALF, spin, frame));
            f.mesh(MeteorShowerFx.ROCK_GLOW, (pose, buffer, cam) -> MeteorShowerFx.cube(pose, buffer, rel, HALF * 1.02F, spin, frame));
            Vec3 toCamera = f.camera.subtract(at);
            Vec3 glow = f.rel(toCamera.lengthSqr() < 1.0E-4 ? at : at.add(toCamera.normalize().scale(HALF * 1.8)));
            f.add(MeteorShowerFx.HEAD[(int)age % MeteorShowerFx.HEAD.length], (pose, buffer) -> LightTrail.flare(pose, buffer, glow, 0.45F, 0xFFFFFF, 0.5F, Vec3.ZERO));
            MagicLights.add(at, MeteorShower.VIOLET, 5.0F, 0.7F);
        } else {
            float i = age - this.flight;
            if (i < 4.0F) {
                float k = 1.0F - i / 4.0F;
                Vec3 back = this.to.subtract(this.dir.scale(0.3));
                f.flare(back, 2.6F * (0.5F + 0.5F * k), MeteorShower.LIGHT, k);
                f.flare(back, 1.2F, 0xFFFFFF, k * k);
            }
            if (i < 8.0F) {
                MagicLights.add(this.to.subtract(this.dir.scale(0.5)), MeteorShower.LIGHT, 6.0F, 0.6F * (1.0F - i / 8.0F));
            }
        }
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        net.minecraft.world.entity.Entity viewer = net.minecraft.client.Minecraft.getInstance().getCameraEntity();
        Vec3 eye = viewer == null ? null : viewer.getEyePosition();
        if (age < this.flight) {
            Vec3 at = this.at(age);
            Vec3 prev = this.at(Math.max(0.0F, age - 1.0F));
            TrailStreaks.emit(prev, at, MeteorShower.VIOLET, 3);
            if (eye != null && at.distanceToSqr(eye) < NEAR * NEAR) {
                return;
            }
            int tongues = 2 + random.nextInt(2);
            for (int t = 0; t < tongues; t++) {
                Vec3 off = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(0.05);
                emit(level, MeteorShowerFx.flame(0.25F + random.nextFloat() * 0.1F, 6).flags(FxOptions.SHRINK).drag(0.85F),
                    at.subtract(this.dir.scale(0.1 + random.nextDouble() * 0.35)).add(off), at.subtract(prev).scale(0.1));
            }
            double length = Math.min(at.distanceTo(this.from) + 0.3, 4.5);
            emit(level, MeteorShowerFx.spark(0.12F + random.nextFloat() * 0.04F, 10), at.subtract(this.dir.scale(length * (0.5 + random.nextDouble() * 0.5))), Vec3.ZERO);
        }
        if (age >= this.flight && !this.burst) {
            this.burst = true;
            Vec3 c = this.to.subtract(this.dir.scale(0.25));
            for (int k = 0; k < 8; k++) {
                Vec3 v = new Vec3(random.nextGaussian(), Math.abs(random.nextGaussian()) + 0.3, random.nextGaussian()).scale(0.12).subtract(this.dir.scale(0.08));
                emit(level, sparkle(k % 2 == 0 ? MeteorShower.PALE : MeteorShower.LIGHT, 0.1F, 22).gravity(0.04F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE), c, v);
            }
            for (int k = 0; k < 5; k++) {
                Vec3 v = new Vec3(random.nextGaussian(), Math.abs(random.nextGaussian()) + 0.5, random.nextGaussian()).scale(0.1).subtract(this.dir.scale(0.06));
                emit(level, MeteorShowerFx.shard(0.14F + random.nextFloat() * 0.06F, 22).gravity(0.05F).drag(0.95F).flags(FxOptions.BOUNCE), c, v);
            }
            for (int k = 0; k < 3; k++) {
                emit(level, MeteorShowerFx.flame(0.3F, 8).flags(FxOptions.SHRINK).drag(0.85F), c,
                    new Vec3(random.nextGaussian() * 0.04, 0.03, random.nextGaussian() * 0.04));
            }
            emit(level, MeteorShowerFx.spark(0.25F, 12), c, Vec3.ZERO);
        }
    }
}
