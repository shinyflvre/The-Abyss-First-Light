package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.InvertedSky;
import net.yezon.theabyss.ring.fx.FxOptions;

final class SkyStarFx extends Effect {
    private static final RenderType[] TRAIL = trail();
    private static final float AFTER = 14.0F;

    private final Vec3 from;
    private final Vec3 to;
    private final Vec3 dir;
    private final float flight;
    private boolean burst;

    SkyStarFx(SpellFxMessage m) {
        this.from = SpellFx.vec(m, 0);
        this.to = SpellFx.vec(m, 3);
        this.flight = Math.max(1.0F, m.ticks());
        Vec3 path = this.to.subtract(this.from);
        this.dir = path.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 1.0, 0.0) : path.normalize();
    }

    private static RenderType[] trail() {
        RenderType[] out = new RenderType[4];
        for (int i = 0; i < out.length; i++) {
            out[i] = LightTrail.type(InvertedSkyFx.texture("star_trail_0" + i));
        }
        return out;
    }

    @Override
    public float life() {
        return this.flight + AFTER;
    }

    private Vec3 at(float age) {
        double t = Mth.clamp(age / this.flight, 0.0F, 1.0F);
        return this.from.lerp(this.to, 1.0 - (1.0 - t) * (1.0 - t));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        if (age < 3.0F) {
            float k = 1.0F - age / 3.0F;
            f.flare(this.from.add(0.0, 0.15, 0.0), 2.4F * k, InvertedSky.CYAN, k);
        }
        if (age < this.flight) {
            Vec3 at = this.at(age);
            double length = Math.min(at.distanceTo(this.from) + 0.2, 4.5);
            int n = 10;
            Vec3[] p = new Vec3[n];
            float[] w = new float[n];
            float[] l = new float[n];
            for (int j = 0; j < n; j++) {
                double s = j / (double)(n - 1);
                p[j] = at.subtract(this.dir.scale(length * s));
                w[j] = (float)(0.5 - 0.4 * s);
                l[j] = Mth.clamp((float)(1.3 - s), 0.0F, 1.0F);
            }
            f.ribbon(TRAIL[(int)(age / 2.0F) % TRAIL.length], p, w, l, 0xFFFFFF);
            int nb = 5;
            Vec3[] pb = new Vec3[nb];
            float[] wb = new float[nb];
            float[] lb = new float[nb];
            for (int j = 0; j < nb; j++) {
                double s = 0.4 * j / (nb - 1);
                pb[j] = at.subtract(this.dir.scale(length * s));
                wb[j] = (float)(0.3 * (0.5 - 0.4 * s));
                lb[j] = (float)Math.pow(Math.max(0.0, 1.0 - s / 0.4), 1.2);
            }
            f.ribbon(LightTrail.STREAK, pb, wb, lb, 0xEAFFFF);
            f.flare(at, 1.4F, InvertedSky.CYAN, 0.9F);
            f.flare(at, 0.6F, 0xFFFFFF, 1.0F);
            MagicLights.add(at, InvertedSky.CYAN, 5.0F, 0.6F);
        } else {
            float i = age - this.flight;
            if (i < 5.0F) {
                float k = 1.0F - i / 5.0F;
                f.flare(this.to, 3.0F * (0.5F + 0.5F * k), InvertedSky.CYAN, k);
                f.flare(this.to, 1.3F, 0xFFFFFF, k * k);
            }
        }
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        if (age < this.flight) {
            Vec3 at = this.at(age);
            Vec3 prev = this.at(Math.max(0.0F, age - 1.0F));
            TrailStreaks.emit(prev, at, InvertedSky.CYAN, 3);
            emit(level, InvertedSkyFx.star(0.22F + random.nextFloat() * 0.1F, 10), at.subtract(this.dir.scale(0.3 + random.nextDouble() * 0.8)), Vec3.ZERO);
        }
        if (age >= this.flight && !this.burst) {
            this.burst = true;
            for (int k = 0; k < 10; k++) {
                Vec3 v = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(0.12);
                emit(level, sparkle(k % 2 == 0 ? InvertedSky.PALE : InvertedSky.CYAN, 0.11F, 20).gravity(0.02F).flags(FxOptions.FLICKER | FxOptions.HOT), this.to, v);
            }
            for (int k = 0; k < 3; k++) {
                emit(level, InvertedSkyFx.star(0.35F, 12), this.to.add(random.nextGaussian() * 0.3, random.nextGaussian() * 0.3, random.nextGaussian() * 0.3), Vec3.ZERO);
            }
        }
    }
}
