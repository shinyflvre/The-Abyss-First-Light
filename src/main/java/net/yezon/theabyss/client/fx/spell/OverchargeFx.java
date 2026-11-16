package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.secondary.Overcharge;

final class OverchargeFx extends Effect {
    private static final int CYAN = 0x6FF3FF;

    private final int player;
    private int charges;
    private float until;

    private OverchargeFx(int player) {
        this.player = player;
    }

    static Effect update(SpellFxMessage m) {
        int id = m.ids().get(0);
        int charges = m.data().get(0).intValue();
        for (Effect e : SpellFx.effects()) {
            if (e instanceof OverchargeFx o && o.player == id) {
                o.charges = charges;
                o.until = o.age() + m.ticks();
                return null;
            }
        }
        if (charges <= 0) {
            return null;
        }
        OverchargeFx fx = new OverchargeFx(id);
        fx.charges = charges;
        fx.until = m.ticks();
        return fx;
    }

    @Override
    public float life() {
        return this.charges <= 0 ? 0.0F : this.until;
    }

    private boolean own(Frame f) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getId() == this.player && mc.options.getCameraType().isFirstPerson();
    }

    static Vec3 orbit(Vec3 feet, double height, float age, int k, int count, boolean own) {
        double a = age * 0.22 + k * Math.PI * 2.0 / Math.max(1, count);
        double r = own ? 0.95 : 1.0;
        double y = (own ? height * 0.93 - 0.6 : height * 0.55) + Math.sin(age * 0.3 + k) * 0.12;
        return feet.add(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    @Override
    public void draw(Frame f) {
        Entity e = f.level.getEntity(this.player);
        if (e == null || this.charges <= 0) {
            return;
        }
        float age = this.age();
        boolean own = this.own(f);
        Vec3 feet = e.getPosition(f.pt);
        RandomSource random = f.level.getRandom();
        float size = own ? 0.6F : 1.0F;
        Vec3[] orbs = new Vec3[this.charges];
        for (int k = 0; k < this.charges; k++) {
            orbs[k] = orbit(feet, e.getBbHeight(), age, k, this.charges, own);
            float flicker = 0.75F + 0.25F * random.nextFloat();
            f.flare(orbs[k], 1.5F * size * flicker, CYAN, 0.9F);
            f.flare(orbs[k], 0.55F * size, 0xFFFFFF, 1.0F);
        }
        for (int k = 0; k < this.charges; k++) {
            if (this.charges > 1 && random.nextInt(3) == 0) {
                f.zap(orbs[k], orbs[(k + 1) % this.charges], 0.18, 0.05F * size, 0.8F, CYAN, random);
            }
            if (random.nextInt(4) == 0) {
                Vec3 body = feet.add(0.0, e.getBbHeight() * (0.3 + random.nextDouble() * 0.5), 0.0);
                f.zap(orbs[k], body, 0.15, 0.04F * size, 0.6F, CYAN, random);
            }
        }
    }

    @Override
    public void tick(ClientLevel level) {
        Entity e = level.getEntity(this.player);
        if (e == null || this.charges <= 0) {
            return;
        }
        RandomSource random = level.getRandom();
        if (random.nextInt(2) == 0) {
            Vec3 at = e.position().add(random.nextGaussian() * 0.3, random.nextDouble() * e.getBbHeight(), random.nextGaussian() * 0.3);
            emit(level, zap(CYAN, 0.18F, 4), at, Vec3.ZERO);
        }
    }

    static final class Shot extends Effect {
        private final int player;
        private final int target;
        private final int left;
        private boolean burst;
        private Vec3 lastAt;

        Shot(SpellFxMessage m) {
            this.player = m.ids().get(0);
            this.target = m.ids().get(1);
            this.left = m.data().get(0).intValue();
        }

        @Override
        public float life() {
            return Overcharge.FLIGHT + 8.0F;
        }

        private void trail(ClientLevel level) {
            float age = this.age();
            Entity p = level.getEntity(this.player);
            Entity t = level.getEntity(this.target);
            if (age >= Overcharge.FLIGHT || p == null || t == null) {
                return;
            }
            Vec3 from = orbit(p.position(), p.getBbHeight(), age, this.left, this.left + 1, false);
            Vec3 at = from.lerp(t.position().add(0.0, t.getBbHeight() * 0.5, 0.0), easeOut(age / Overcharge.FLIGHT));
            if (this.lastAt != null) {
                TrailStreaks.emit(this.lastAt, at, CYAN, 3);
            }
            this.lastAt = at;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            Entity p = f.level.getEntity(this.player);
            Vec3 to = f.body(this.target);
            if (p == null || to == null) {
                return;
            }
            Vec3 from = orbit(p.getPosition(f.pt), p.getBbHeight(), age, this.left, this.left + 1, false);
            float t = (float)Math.min(1.0, age / Overcharge.FLIGHT);
            if (t < 1.0F) {
                Vec3 at = from.lerp(to, easeOut(t));
                f.line(from, at, 0.3F, 0.8F, CYAN);
                f.flare(at, 1.6F, CYAN, 1.0F);
                f.flare(at, 0.6F, 0xFFFFFF, 1.0F);
            } else {
                float k = 1.0F - (age - Overcharge.FLIGHT) / 8.0F;
                f.flare(to, 4.0F * k, CYAN, k);
                f.zap(to.add(0.0, 6.0, 0.0), to, 0.6, 0.25F * k, k, CYAN, f.level.getRandom());
            }
        }

        @Override
        public void tick(ClientLevel level) {
            this.trail(level);
            if (this.burst || this.age() < Overcharge.FLIGHT) {
                return;
            }
            this.burst = true;
            Entity t = level.getEntity(this.target);
            if (t == null) {
                return;
            }
            RandomSource random = level.getRandom();
            for (int k = 0; k < 16; k++) {
                emit(level, sparkle(CYAN, 0.13F, 20).gravity(0.05F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
                    t.position().add(0.0, t.getBbHeight() * 0.5, 0.0),
                    new Vec3(random.nextGaussian() * 0.15, 0.2 + random.nextDouble() * 0.2, random.nextGaussian() * 0.15));
            }
        }
    }
}
