package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.secondary.CrystalWall;

final class WallFx extends Effect {
    private static final int CYAN = 0x6FE8FF;
    private static final int ICE = 0xB5F7FF;

    private final Vec3 centre;
    private final Vec3 along;
    private final int life;

    WallFx(SpellFxMessage m) {
        this.centre = SpellFx.vec(m, 0);
        this.along = new Vec3(m.data().get(3), 0.0, m.data().get(4)).normalize();
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    private double open(float age) {
        double reach = (age - 2.0) / 2.0 * CrystalWall.GAP;
        return Math.max(0.0, Math.min(CrystalWall.halfLength(), reach));
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        double open = this.open(age);
        if (open <= 0.05) {
            return;
        }
        float fade = this.fade(0.0F, 8.0F);
        int n = Math.max(2, (int)(open * 2.0 / 0.4));
        Vec3[] bottom = new Vec3[n + 1];
        Vec3[] top = new Vec3[n + 1];
        float[] light = new float[n + 1];
        int[] rgb = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            double s = -open + 2.0 * open * i / n;
            double edge = 1.0 - Math.pow(Math.abs(s) / CrystalWall.halfLength(), 6.0);
            Vec3 base = this.centre.add(this.along.scale(s)).add(0.0, 0.05, 0.0);
            bottom[i] = base;
            top[i] = base.add(0.0, CrystalWall.HEIGHT * (0.85 + 0.15 * Math.sin(s * 2.1 + age * 0.2)), 0.0);
            light[i] = (float)(0.3 + 0.12 * Math.sin(s * 3.0 - age * 0.35)) * fade * (float)edge;
            rgb[i] = CYAN;
        }
        Vec3[] bottomRel = new Vec3[n + 1];
        Vec3[] topRel = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            bottomRel[i] = f.rel(bottom[i]);
            topRel[i] = f.rel(top[i]);
        }
        f.add(LightTrail.SHEET, (pose, buffer) -> LightTrail.band(pose, buffer, bottomRel, topRel, light, rgb));
        f.ribbon(bottom, 0.45F, 0.8F * fade, CYAN);
        f.ribbon(bottom, 0.12F, fade, 0xFFFFFF);
        int spikes = CrystalWall.SPIKES;
        int mid = spikes / 2;
        for (int i = 0; i + 1 < spikes; i++) {
            double s0 = (i - mid) * CrystalWall.GAP;
            double s1 = (i + 1 - mid) * CrystalWall.GAP;
            if (Math.abs(s0) > open || Math.abs(s1) > open) {
                continue;
            }
            Vec3 a = this.centre.add(this.along.scale(s0)).add(0.0, i % 2 == 0 ? 2.1 : 1.3, 0.0);
            Vec3 b = this.centre.add(this.along.scale(s1)).add(0.0, i % 2 == 0 ? 1.3 : 2.1, 0.0);
            f.line(a, b, 0.06F, 0.8F * fade, ICE);
        }
        for (int k = 0; k < 5; k++) {
            double s = Math.sin(k * 7.3 + Math.floor(age / 12.0) * 3.1) * open;
            double rise = ((age + k * 2.4) % 12.0) / 12.0;
            Vec3 base = this.centre.add(this.along.scale(s));
            Vec3 a = base.add(0.0, CrystalWall.HEIGHT * Math.max(0.0, rise - 0.25), 0.0);
            Vec3 b = base.add(0.0, CrystalWall.HEIGHT * rise, 0.0);
            f.line(a, b, 0.1F, (float)(1.0 - rise) * fade, ICE);
        }
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        double open = this.open(age);
        if (open <= 0.05 || age > this.life - 6) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int k = 0; k < 2; k++) {
            double s = (random.nextDouble() * 2.0 - 1.0) * open;
            Vec3 at = this.centre.add(this.along.scale(s)).add(0.0, random.nextDouble() * CrystalWall.HEIGHT, 0.0);
            emit(level, sparkle(k == 0 ? ICE : CYAN, 0.11F, 24).gravity(0.004F), at, new Vec3(0.0, -0.01, 0.0));
        }
    }
}
