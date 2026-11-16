package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;

final class HexFx extends Effect {
    private static final int CURSE = 0xFF3DD8;
    private static final int DEEP = 0x7A2BB0;
    private static final int PALE = 0xFFC8F4;
    private static final float BOLT = 6.0F;

    private final int target;
    private final Vec3 source;
    private final int life;

    HexFx(SpellFxMessage m) {
        this.target = m.ids().get(0);
        this.source = SpellFx.vec(m, 0);
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public int outline(Entity entity) {
        return entity.getId() == this.target && this.age() > 2.0F ? 0xFFFF3DD8 : 0;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        Entity e = f.level.getEntity(this.target);
        if (e == null || !e.isAlive()) {
            return;
        }
        Vec3 feet = e.getPosition(f.pt);
        Vec3 body = feet.add(0.0, e.getBbHeight() * 0.55, 0.0);
        if (age < BOLT) {
            float k = 1.0F - age / BOLT;
            f.zap(this.source, body, 0.5, 0.22F, k, CURSE, f.level.getRandom());
            f.flare(body, 3.0F * k + 1.0F, CURSE, k);
        }
        float fade = this.fade(4.0F, 20.0F);
        float pulse = 0.75F + 0.25F * (float)Math.sin(age * 0.45);
        double r = e.getBbWidth() * 0.5 + 0.9;
        Vec3 ground = feet.add(0.0, 0.06, 0.0);
        double turn = age * 0.04;
        for (int tri = 0; tri < 2; tri++) {
            Vec3[] p = new Vec3[4];
            for (int i = 0; i <= 3; i++) {
                double a = turn + tri * Math.PI / 3.0 + i * Math.PI * 2.0 / 3.0;
                p[i] = ground.add(Math.cos(a) * r * 0.95, 0.0, Math.sin(a) * r * 0.95);
            }
            f.ribbon(p, 0.09F, fade * pulse, CURSE);
        }
        double tilt = 0.5 + 0.15 * Math.sin(age * 0.07);
        double yaw = age * 0.09;
        Vec3 u = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
        Vec3 v = new Vec3(-Math.sin(yaw) * Math.cos(tilt), Math.sin(tilt), Math.cos(yaw) * Math.cos(tilt));
        f.circle(body, u, v, r * 0.85, 0.08F, fade * 0.85F, PALE);
        for (int i = 0; i < 3; i++) {
            double a = age * 0.16 + i * Math.PI * 2.0 / 3.0;
            Vec3 at = body.add(Math.cos(a) * r * 0.9, Math.sin(age * 0.11 + i) * e.getBbHeight() * 0.35, Math.sin(a) * r * 0.9);
            f.flare(at, 0.9F, CURSE, fade);
            f.flare(at, 0.3F, 0xFFFFFF, fade);
        }
    }

    @Override
    public void tick(ClientLevel level) {
        Entity e = level.getEntity(this.target);
        if (e == null || this.age() > this.life - 10) {
            return;
        }
        if (this.age() < BOLT) {
            TrailStreaks.emit(this.source, e.position().add(0.0, e.getBbHeight() * 0.55, 0.0), CURSE, 4);
        }
        RandomSource random = level.getRandom();
        if (random.nextInt(3) == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = e.getBbWidth() * 0.5 + 0.9;
            emit(level, wisp(random.nextBoolean() ? CURSE : DEEP, 0.18F, 18).gravity(-0.004F), e.position().add(Math.cos(a) * r, 0.1, Math.sin(a) * r),
                new Vec3(0.0, 0.03, 0.0));
        }
    }

    static final class Shatter extends Effect {
        private final Vec3 at;
        private final Vec3[] shards;
        private boolean burst;

        Shatter(SpellFxMessage m) {
            this.at = SpellFx.vec(m, 0);
            RandomSource random = RandomSource.create((long)(this.at.x * 13 + this.at.z * 7));
            this.shards = new Vec3[14];
            for (int i = 0; i < this.shards.length; i++) {
                this.shards[i] = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.6 + 0.3, random.nextGaussian()).normalize();
            }
        }

        @Override
        public float life() {
            return 16.0F;
        }

        @Override
        public void draw(Frame f) {
            float age = this.age();
            float k = 1.0F - age / this.life();
            double e = easeOut(age / 10.0);
            for (Vec3 d : this.shards) {
                Vec3 a = this.at.add(d.scale(0.4 + 3.2 * e));
                Vec3 b = this.at.add(d.scale(0.4 + 3.2 * e + 0.9 * k));
                f.line(a, b, 0.12F * k + 0.02F, k, CURSE);
            }
            f.flare(this.at, 6.0F * k, CURSE, k);
            f.flare(this.at, 2.5F * k, 0xFFFFFF, k * k);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.burst) {
                return;
            }
            this.burst = true;
            RandomSource random = level.getRandom();
            for (int k = 0; k < 30; k++) {
                Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                emit(level, k % 2 == 0 ? wisp(CURSE, 0.25F, 20) : sparkle(PALE, 0.14F, 22).flags(FxOptions.FLICKER), this.at, d.scale(0.25 + random.nextDouble() * 0.25));
            }
        }
    }
}
