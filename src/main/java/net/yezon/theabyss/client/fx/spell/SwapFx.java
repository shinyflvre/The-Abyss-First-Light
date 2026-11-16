package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class SwapFx extends Effect {
    private static final int LILAC = 0xC890FF;
    private static final int CYAN = 0x7FE8FF;
    private static final float FLIGHT = 9.0F;

    private final Vec3 a;
    private final Vec3 b;
    private final Vec3[] arcAB;
    private final Vec3[] arcBA;
    private final int life;
    private boolean burst;

    SwapFx(SpellFxMessage m) {
        this.a = SpellFx.vec(m, 0);
        this.b = SpellFx.vec(m, 3);
        this.life = m.ticks();
        Vec3 mid = this.a.add(this.b).scale(0.5);
        Vec3 span = this.b.subtract(this.a);
        Vec3 side = span.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        double lift = 1.5 + span.length() * 0.18;
        this.arcAB = Frame.curve(this.a, mid.add(0.0, lift, 0.0).add(side.scale(lift * 0.6)), this.b, 28);
        this.arcBA = Frame.curve(this.b, mid.add(0.0, lift, 0.0).subtract(side.scale(lift * 0.6)), this.a, 28);
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        this.comet(f, this.arcAB, age, LILAC);
        this.comet(f, this.arcBA, age, CYAN);
        float fade = this.fade(2.0F, 10.0F);
        this.rift(f, this.a, age, fade, CYAN);
        this.rift(f, this.b, age, fade, LILAC);
    }

    private void comet(Frame f, Vec3[] arc, float age, int colour) {
        int n = arc.length;
        float head = Math.min(1.0F, age / FLIGHT);
        float headEase = (float)smooth(head);
        int last = Math.max(1, Math.round(headEase * (n - 1)));
        Vec3[] p = new Vec3[last + 1];
        float[] w = new float[last + 1];
        float[] l = new float[last + 1];
        float linger = Math.max(0.0F, 1.0F - Math.max(0.0F, age - FLIGHT) / (this.life - FLIGHT));
        for (int i = 0; i <= last; i++) {
            p[i] = arc[last - i];
            float behind = i / (float)Math.max(1, last);
            w[i] = 0.5F * (1.0F - behind) + 0.12F;
            l[i] = Math.max(linger * 0.55F, (1.0F - behind) * (1.0F - behind)) * (age < FLIGHT ? 1.0F : linger);
        }
        f.ribbon(p, w, l, colour);
        float[] core = new float[last + 1];
        for (int i = 0; i <= last; i++) {
            core[i] = w[i] * 0.3F;
        }
        f.ribbon(p, core, l, 0xFFFFFF);
        if (age < FLIGHT + 2.0F) {
            Vec3 at = arc[last];
            f.flare(at, 2.2F, colour, 1.0F);
            f.flare(at, 0.8F, 0xFFFFFF, 1.0F);
        }
    }

    private void rift(Frame f, Vec3 centre, float age, float fade, int colour) {
        Vec3 toCam = f.camera.subtract(centre).multiply(1.0, 0.0, 1.0);
        Vec3 side = toCam.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-toCam.z, 0.0, toCam.x).normalize();
        float open = (float)easeOut(age / 4.0) * fade;
        int n = 24;
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            double t = i * Math.PI * 2.0 / n;
            p[i] = centre.add(0.0, Math.cos(t) * 1.25, 0.0).add(side.scale(Math.sin(t) * 0.45 * open));
        }
        f.ribbon(p, 0.16F, open, colour);
        f.ribbon(p, 0.05F, open, 0xFFFFFF);
        f.flare(centre, 2.6F * open, colour, 0.5F * open);
    }

    @Override
    public void tick(ClientLevel level) {
        if (this.burst) {
            return;
        }
        this.burst = true;
        RandomSource random = level.getRandom();
        for (Vec3 c : new Vec3[] {this.a, this.b}) {
            for (int k = 0; k < 18; k++) {
                double ang = random.nextDouble() * Math.PI * 2.0;
                Vec3 v = new Vec3(Math.cos(ang) * 0.12, (random.nextDouble() - 0.3) * 0.12, Math.sin(ang) * 0.12);
                emit(level, sparkle(k % 2 == 0 ? LILAC : CYAN, 0.13F, 22).drag(0.9F), c.add(0.0, (random.nextDouble() - 0.5) * 1.6, 0.0), v);
            }
        }
    }
}
