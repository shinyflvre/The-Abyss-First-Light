package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.InvertedSky;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.secondary.AuroraCage;

final class AuroraCageFx extends Effect {
    static final RenderType[] CURTAIN = curtain();
    private static final int SEGMENTS = 16;
    private static final int RISERS = 120;

    private final Vec3 centre;
    private final double radius;
    private final double height;
    private final float span;
    private final List<Riser> risers = new ArrayList<>();

    AuroraCageFx(SpellFxMessage m) {
        this.centre = SpellFx.vec(m, 0);
        this.radius = m.data().size() > 3 ? m.data().get(3) : AuroraCage.RADIUS;
        this.height = m.data().size() > 4 ? m.data().get(4) : AuroraCage.HEIGHT;
        this.span = Math.max(20.0F, m.ticks());
    }

    private static RenderType[] curtain() {
        RenderType[] out = new RenderType[6];
        for (int i = 0; i < out.length; i++) {
            out[i] = LightTrail.type(InvertedSkyFx.texture("aurora_curtain_" + i));
        }
        return out;
    }

    @Override
    public float life() {
        return this.span + 10.0F;
    }

    private float strength(float age) {
        float in = (float)smooth(age / AuroraCage.RISE);
        float out = (float)smooth((this.span + 10.0F - age) / 20.0F);
        return Math.max(0.0F, Math.min(in, out));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float k = this.strength(age);
        if (k > 0.01F) {
            Vec3[] top = new Vec3[SEGMENTS + 1];
            Vec3[] bottom = new Vec3[SEGMENTS + 1];
            float[] light = new float[SEGMENTS + 1];
            int[] rgb = new int[SEGMENTS + 1];
            for (int s = 0; s <= SEGMENTS; s++) {
                double a = s * Math.PI * 2.0 / SEGMENTS;
                double wave = Math.sin(a * 3.0 + age * 0.12) * 0.35 + Math.sin(a * 7.0 - age * 0.2) * 0.15;
                double r = this.radius + wave * 0.4;
                double h = this.height * k * (0.85 + 0.15 * Math.sin(a * 5.0 + age * 0.15));
                Vec3 base = this.centre.add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
                bottom[s] = f.rel(base);
                top[s] = f.rel(base.add(Math.cos(a) * wave * 0.3, h, Math.sin(a) * wave * 0.3));
                light[s] = k * (0.8F + 0.2F * (float)Math.sin(a * 4.0 + age * 0.3));
                rgb[s] = 0xFFFFFF;
            }
            glow(f, bottom, top, light);
            f.add(CURTAIN[(int)(age / 3.0F) % CURTAIN.length], (pose, buffer) -> LightTrail.band(pose, buffer, top, bottom, light, rgb));
            MagicLights.add(this.centre.add(0.0, 1.5, 0.0), 0x12A9B8, (float)(this.radius * 2.4), 0.75F * k);
            MagicLights.add(this.centre.add(0.0, this.height, 0.0), 0x8D45DE, (float)(this.radius * 1.6), 0.35F * k);
        }
        for (Riser s : this.risers) {
            float u = Math.min(1.0F, (s.age + f.pt) / s.life);
            float head = (1.0F - (1.0F - u) * (1.0F - u)) * s.height;
            float tail = Math.max(0.0F, head - s.height * 0.5F);
            float a = Math.min(1.0F, u * 6.0F) * Math.min(1.0F, (1.0F - u) * 3.0F);
            Vec3 high = s.base.add(0.0, head, 0.0);
            Vec3 low = s.base.add(0.0, tail, 0.0);
            f.ribbon(new Vec3[] {low, low.lerp(high, 0.6), high}, new float[] {0.012F, 0.05F, 0.07F}, new float[] {0.0F, a * 0.55F, a}, s.rgb);
            f.flare(high, 0.2F, s.rgb, a * 0.85F);
        }
    }

    static void glow(Frame f, Vec3[] bottom, Vec3[] top, float[] light) {
        int n = bottom.length;
        Vec3[] low = new Vec3[n];
        Vec3[] mid = new Vec3[n];
        float[] lowLight = new float[n];
        int[] cyan = new int[n];
        for (int i = 0; i < n; i++) {
            Vec3 up = top[i].subtract(bottom[i]);
            low[i] = bottom[i].subtract(up.scale(0.08));
            mid[i] = bottom[i].add(up.scale(0.6));
            lowLight[i] = light[i] * 0.4F;
            cyan[i] = 0x3FE6F0;
        }
        f.add(LightTrail.SHEET, (pose, buffer) -> LightTrail.band(pose, buffer, mid, low, lowLight, cyan));
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        this.risers.removeIf(s -> ++s.age >= s.life);
        float k = this.strength(age);
        if (k < 0.1F) {
            return;
        }
        for (int i = 0; i < 2 && this.risers.size() < RISERS; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            Riser s = new Riser();
            s.base = this.centre.add(Math.cos(a) * this.radius, 0.05, Math.sin(a) * this.radius);
            s.rgb = i == 0 ? InvertedSky.AURORA : InvertedSky.VIOLET;
            s.height = 2.0F + random.nextFloat() * 3.0F;
            s.life = 12 + random.nextInt(14);
            this.risers.add(s);
        }
        double a = random.nextDouble() * Math.PI * 2.0;
        double d = Math.sqrt(random.nextDouble()) * this.radius;
        emit(level, InvertedSkyFx.mote(0.15F + random.nextFloat() * 0.08F, 30),
            this.centre.add(Math.cos(a) * d, 0.5 + random.nextDouble() * this.height, Math.sin(a) * d), new Vec3(0.0, 0.015, 0.0));
        if (random.nextInt(2) == 0) {
            double b = random.nextDouble() * Math.PI * 2.0;
            emit(level, sparkle(InvertedSky.PALE, 0.1F, 16).flags(FxOptions.FLICKER | FxOptions.HOT),
                this.centre.add(Math.cos(b) * this.radius, random.nextDouble() * this.height * k, Math.sin(b) * this.radius), new Vec3(0.0, 0.04, 0.0));
        }
    }

    private static final class Riser {
        Vec3 base;
        int rgb;
        float height;
        int life;
        int age;
    }
}
