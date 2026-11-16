package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;

final class RewindFx extends Effect {
    private static final int GOLD = 0xFFD36A;
    private static final int CYAN = 0x7FE8FF;
    private static final float RUN = 14.0F;

    private final Vec3[] path;
    private final int life;

    RewindFx(SpellFxMessage m) {
        int n = (m.data().size() - 2) / 3;
        this.path = new Vec3[n];
        for (int i = 0; i < n; i++) {
            this.path[i] = SpellFx.vec(m, 2 + i * 3).add(0.0, 0.9, 0.0);
        }
        this.life = m.ticks();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !m.ids().isEmpty() && mc.player.getId() == m.ids().get(0) && n >= 2) {
            Vec3[] eyes = new Vec3[n];
            for (int i = 0; i < n; i++) {
                eyes[i] = this.path[i].add(0.0, mc.player.getEyeHeight() - 0.9, 0.0);
            }
            RewindCamera.start(eyes, mc.player.getYRot(), mc.player.getXRot());
        }
    }

    @Override
    public float life() {
        return this.life;
    }

    private float head(float age) {
        return (float)smooth(age / RUN) * (this.path.length - 1);
    }

    private Vec3 at(float index) {
        int i = Math.min(this.path.length - 2, (int)index);
        return this.path[i].lerp(this.path[i + 1], index - i);
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        int n = this.path.length;
        float head = this.head(age);
        float after = Math.max(0.0F, 1.0F - Math.max(0.0F, age - RUN) / (this.life - RUN));
        int from = (int)Math.ceil(head);
        if (from < n) {
            Vec3[] p = new Vec3[n - from + 1];
            p[0] = this.at(head);
            System.arraycopy(this.path, from, p, 1, n - from);
            float in = Math.min(1.0F, age / 3.0F);
            f.ribbon(p, 0.22F, 0.8F * in * after, GOLD);
            f.ribbon(p, 0.06F, in * after, 0xFFFFFF);
            for (int i = from; i < n; i += 3) {
                float flicker = 0.6F + 0.4F * (float)Math.sin(age * 1.7 + i);
                f.flare(this.path[i], 1.4F, CYAN, 0.55F * flicker * in);
            }
        }
        if (age < RUN + 2.0F) {
            Vec3 c = this.at(Math.min(head, n - 1.001F));
            int tail = 8;
            Vec3[] p = new Vec3[tail];
            float[] w = new float[tail];
            float[] l = new float[tail];
            for (int k = 0; k < tail; k++) {
                float idx = Math.max(0.0F, head - k * 0.6F);
                p[k] = this.at(Math.min(idx, n - 1.001F));
                w[k] = 0.7F * (1.0F - k / (float)tail);
                l[k] = 1.0F - k / (float)tail;
            }
            f.ribbon(p, w, l, CYAN);
            f.flare(c, 2.4F, CYAN, 1.0F);
            f.flare(c, 0.9F, 0xFFFFFF, 1.0F);
        }
        this.clock(f, this.path[0].subtract(0.0, 0.83, 0.0), age, Math.max(0.0F, 1.0F - age / 16.0F), GOLD);
        this.clock(f, this.path[n - 1].subtract(0.0, 0.83, 0.0), age, Math.min(1.0F, age / 6.0F) * this.fade(0.0F, 10.0F), CYAN);
    }

    private void clock(Frame f, Vec3 c, float age, float light, int colour) {
        if (light <= 0.01F) {
            return;
        }
        double r = 1.6;
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6.0;
            Vec3 d = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            f.line(c.add(d.scale(r * (i % 3 == 0 ? 0.62 : 0.72))), c.add(d.scale(r * 0.9)), i % 3 == 0 ? 0.1F : 0.06F, light, colour);
        }
        double minute = -age * 0.55;
        double hour = -age * 0.07 + 1.0;
        f.line(c, c.add(Math.cos(minute) * r * 0.85, 0.0, Math.sin(minute) * r * 0.85), 0.08F, light, 0xFFFFFF);
        f.line(c, c.add(Math.cos(hour) * r * 0.55, 0.0, Math.sin(hour) * r * 0.55), 0.12F, light, colour);
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        if (age > RUN + 1.0F) {
            return;
        }
        RandomSource random = level.getRandom();
        Vec3 c = this.at(Math.min(this.head(age), this.path.length - 1.001F));
        for (int k = 0; k < 3; k++) {
            emit(level, sparkle(k == 0 ? CYAN : 0xFFD36A, 0.12F, 22).gravity(-0.002F).flags(FxOptions.FLICKER),
                c.add(random.nextGaussian() * 0.25, random.nextGaussian() * 0.4, random.nextGaussian() * 0.25), new Vec3(0.0, 0.02, 0.0));
        }
    }
}
