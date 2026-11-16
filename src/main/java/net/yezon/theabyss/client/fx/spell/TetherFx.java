package net.yezon.theabyss.client.fx.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class TetherFx extends Effect {
    private static final int ORANGE = 0xFF9A3C;
    private static final int PALE = 0xFFE0B0;
    private static final int[][] EDGES = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6}, {6, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    private final int caster;
    private final int target;
    private final int life;

    TetherFx(SpellFxMessage m) {
        this.caster = m.ids().get(0);
        this.target = m.ids().get(1);
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public int outline(Entity entity) {
        return entity.getId() == this.target ? 0xFFFF9A3C : 0;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float fade = this.fade(3.0F, 6.0F);
        Vec3 from = f.body(this.caster);
        Vec3 to = f.body(this.target);
        if (from != null && to != null) {
            Vec3 hand = from.add(0.0, 0.3, 0.0);
            int n = 20;
            Vec3 span = to.subtract(hand);
            Vec3 side = span.cross(new Vec3(0.0, 1.0, 0.0));
            side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
            Vec3[] p = new Vec3[n + 1];
            for (int i = 0; i <= n; i++) {
                double s = i / (double)n;
                double wave = Math.sin(s * Math.PI * 3.0 - age * 0.8) * 0.18 * Math.sin(s * Math.PI);
                p[i] = hand.add(span.scale(s)).add(side.scale(wave)).add(0.0, Math.sin(s * Math.PI) * 0.4, 0.0);
            }
            f.ribbon(p, 0.32F, 0.75F * fade, ORANGE);
            f.ribbon(p, 0.08F, fade, PALE);
            double pulse = (age * 0.12) % 1.0;
            Vec3 at = p[(int)Math.round(pulse * n)];
            f.flare(at, 1.4F, ORANGE, fade);
        }
        if (to != null) {
            Entity e = f.level.getEntity(this.target);
            double h = e == null ? 1.0 : e.getBbHeight() * 0.5 + 0.3;
            double w = e == null ? 0.6 : e.getBbWidth() * 0.5 + 0.3;
            double fold = 1.0 + 0.6 * (1.0 - easeOut(age / 8.0));
            double turn = age * 0.08;
            Vec3[] corner = new Vec3[8];
            for (int i = 0; i < 8; i++) {
                double x = ((i & 1) == 0 ? -w : w) * fold;
                double y = ((i & 2) == 0 ? -h : h) * fold;
                double z = ((i & 4) == 0 ? -w : w) * fold;
                double rx = x * Math.cos(turn) - z * Math.sin(turn);
                double rz = x * Math.sin(turn) + z * Math.cos(turn);
                corner[i] = to.add(rx, y, rz);
            }
            for (int[] edge : EDGES) {
                f.line(corner[edge[0]], corner[edge[1]], 0.1F, fade, ORANGE);
            }
            for (Vec3 c : corner) {
                f.flare(c, 0.7F, PALE, fade);
            }
        }
    }
}
