package net.yezon.theabyss.client.fx.spell;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class PuppetFx extends Effect {
    private static final int CURSE = 0xFF3DD8;
    private static final int PALE = 0xFFC8F4;
    private static final int RED = 0xFF4040;

    private final int caster;
    private final int mob;
    private final int life;
    private int victim = -1;

    PuppetFx(SpellFxMessage m) {
        this.caster = m.ids().get(0);
        this.mob = m.ids().get(1);
        this.life = m.ticks();
    }

    static void retarget(SpellFxMessage m) {
        for (Effect e : SpellFx.effects()) {
            if (e instanceof PuppetFx p && p.mob == m.ids().get(0)) {
                p.victim = m.ids().get(1);
            }
        }
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public int outline(Entity entity) {
        if (entity.getId() == this.mob) {
            return 0xFFFF3DD8;
        }
        return entity.getId() == this.victim ? 0xFFFF4040 : 0;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        Entity e = f.level.getEntity(this.mob);
        if (e == null || !e.isAlive()) {
            return;
        }
        float fade = this.fade(4.0F, 8.0F);
        Vec3 feet = e.getPosition(f.pt);
        double h = e.getBbHeight();
        double w = e.getBbWidth() * 0.5;
        float yaw = (e instanceof LivingEntity living ? living.getPreciseBodyRotation(f.pt) : Mth.lerp(f.pt, e.yRotO, e.getYRot())) * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        double sway = Math.sin(age * 0.18) * 0.25;
        Vec3 cross = feet.add(0.0, h + 1.5 + Math.sin(age * 0.11) * 0.08, 0.0).add(right.scale(sway));
        Vec3 barA = right.scale(w + 0.55).add(0.0, sway * 0.4, 0.0);
        Vec3 barB = forward.scale(w + 0.4);
        f.line(cross.subtract(barA), cross.add(barA), 0.16F, fade, CURSE);
        f.line(cross.subtract(barB), cross.add(barB), 0.16F, fade, CURSE);
        f.line(cross.subtract(barA), cross.add(barA), 0.05F, fade, 0xFFFFFF);
        f.line(cross.subtract(barB), cross.add(barB), 0.05F, fade, 0xFFFFFF);
        f.flare(cross, 1.8F, CURSE, 0.7F * fade);
        double jerk = Math.sin(age * 0.6) * 0.12;
        Vec3[][] strings = {
            {cross, feet.add(0.0, h, 0.0)},
            {cross.add(barA), feet.add(0.0, h * 0.62 + jerk, 0.0).add(right.scale(w + 0.1))},
            {cross.subtract(barA), feet.add(0.0, h * 0.62 - jerk, 0.0).subtract(right.scale(w + 0.1))},
            {cross.add(barB), feet.add(0.0, h * 0.12 - jerk, 0.0).add(right.scale(w * 0.5))},
            {cross.subtract(barB), feet.add(0.0, h * 0.12 + jerk, 0.0).subtract(right.scale(w * 0.5))}
        };
        for (Vec3[] s : strings) {
            f.line(s[0], s[1], 0.05F, fade, PALE);
            f.line(s[0], s[1], 0.18F, 0.45F * fade, CURSE);
        }
        Vec3 hand = f.body(this.caster);
        if (hand != null) {
            Vec3 a = hand.add(0.0, 0.35, 0.0);
            Vec3 mid = a.add(cross).scale(0.5).add(0.0, 1.0, 0.0);
            f.ribbon(Frame.curve(a, mid, cross, 18), 0.04F, 0.6F * fade, PALE);
        }
        Vec3 target = this.victim >= 0 ? f.body(this.victim) : null;
        if (target != null) {
            Vec3 eyes = feet.add(0.0, h * 0.85, 0.0);
            float pulse = 0.55F + 0.45F * (float)Math.sin(age * 0.7);
            f.line(eyes, target, 0.18F, 0.65F * pulse * fade, RED);
            f.line(eyes, target, 0.05F, pulse * fade, 0xFFFFFF);
            f.flare(target, 1.6F, RED, 0.7F * pulse * fade);
        }
    }
}
