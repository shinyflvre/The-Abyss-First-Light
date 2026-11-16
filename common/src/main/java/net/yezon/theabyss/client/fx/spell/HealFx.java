package net.yezon.theabyss.client.fx.spell;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.secondary.HealingPulse;

final class HealFx extends Effect {
    private static final int GREEN = 0x7CFF6A;
    private static final int GOLD = 0xFFE45C;

    private final int caster;
    private final List<Integer> friends;
    private final int life;
    private boolean landed;

    HealFx(SpellFxMessage m) {
        this.caster = m.ids().get(0);
        this.friends = m.ids().subList(1, m.ids().size());
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public int outline(Entity entity) {
        return this.age() > HealingPulse.TRAVEL && this.age() < this.life - 6 && this.friends.contains(entity.getId()) ? 0xFF7CFF6A : 0;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        Vec3 from = f.body(this.caster);
        if (from == null) {
            return;
        }
        Vec3 feet = from.subtract(0.0, 0.9, 0.0);
        float fade = this.fade(2.0F, 10.0F);
        float col = (float)Math.max(0.0, 1.0 - age / 14.0);
        f.line(feet, feet.add(0.0, 4.5 * easeOut(age / 5.0), 0.0), 1.2F * col, col, GREEN);
        f.line(feet, feet.add(0.0, 4.0 * easeOut(age / 5.0), 0.0), 0.35F * col, col, 0xFFFFFF);
        float travel = (float)Math.min(1.0, age / HealingPulse.TRAVEL);
        for (int id : this.friends) {
            Vec3 to = f.body(id);
            if (to == null) {
                continue;
            }
            Vec3 lift = from.add(to).scale(0.5).add(0.0, 2.0 + from.distanceTo(to) * 0.2, 0.0);
            Vec3[] arc = Frame.curve(from.add(0.0, 0.6, 0.0), lift, to, 22);
            int head = Math.max(1, Math.round((float)smooth(travel) * (arc.length - 1)));
            Vec3[] p = new Vec3[head + 1];
            float[] w = new float[head + 1];
            float[] l = new float[head + 1];
            for (int i = 0; i <= head; i++) {
                p[i] = arc[head - i];
                float s = i / (float)Math.max(1, head);
                w[i] = 0.45F * (1.0F - s) + 0.08F;
                l[i] = (1.0F - s) * fade + 0.25F * fade;
            }
            f.ribbon(p, w, l, GOLD);
            if (travel < 1.0F) {
                f.flare(arc[head], 1.6F, GOLD, 1.0F);
            }
            if (age > HealingPulse.TRAVEL) {
                float t = (float)Math.min(1.0, (age - HealingPulse.TRAVEL) / 12.0);
                f.flare(to, 2.5F * (1.0F - t), GREEN, 0.6F * (1.0F - t));
            }
        }
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        RandomSource random = level.getRandom();
        if (age > HealingPulse.TRAVEL && !this.landed) {
            this.landed = true;
            for (int id : this.friends) {
                Entity e = level.getEntity(id);
                if (e == null) {
                    continue;
                }
                for (int k = 0; k < 16; k++) {
                    Vec3 at = e.position().add(random.nextGaussian() * 0.4, random.nextDouble() * e.getBbHeight(), random.nextGaussian() * 0.4);
                    emit(level, sparkle(k % 2 == 0 ? GREEN : GOLD, 0.13F, 26).gravity(-0.004F).flags(FxOptions.FLICKER), at, new Vec3(0.0, 0.03, 0.0));
                }
            }
        }
        if (age < 10.0F) {
            Entity c = level.getEntity(this.caster);
            if (c != null) {
                for (int k = 0; k < 3; k++) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    emit(level, sparkle(k == 0 ? GOLD : GREEN, 0.12F, 22).gravity(-0.005F), c.position().add(Math.cos(a) * 0.6, 0.1, Math.sin(a) * 0.6),
                        new Vec3(0.0, 0.06, 0.0));
                }
            }
        }
    }
}
