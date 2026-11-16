package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.SpellCircle;

final class RecallFx extends Effect {
    static final int ABYSS = 0x5AD7FF;
    static final int PALE = 0xD8F6FF;

    private final Vec3 from;
    private final Vec3 to;
    private final int life;

    RecallFx(SpellFxMessage m) {
        this.from = SpellFx.vec(m, 0);
        this.to = SpellFx.vec(m, 3);
        this.life = m.ticks();
        Mark.clear(m.ids().get(0));
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float k = this.fade(0.0F, this.life * 0.8F);
        for (Vec3 at : new Vec3[] {this.from, this.to}) {
            double up = 30.0 * easeOut(age / 6.0);
            f.line(at, at.add(0.0, up, 0.0), 2.2F * k, k, ABYSS);
            f.line(at, at.add(0.0, up, 0.0), 0.5F * k, k, 0xFFFFFF);
        }
    }

    static final class Mark extends Effect {
        private final int player;
        private final Vec3 at;
        private final int life;
        private final SpellCircleFx circle;

        Mark(SpellFxMessage m) {
            this.player = m.ids().get(0);
            clear(this.player);
            this.at = m.data().isEmpty() ? null : SpellFx.vec(m, 0);
            this.life = m.ticks();
            this.circle = this.at == null ? null : new SpellCircleFx(SpellCircle.TECH, this.at, -1, ABYSS, 1.4F, this.life, 0.0F);
        }

        static void clear(int player) {
            for (Effect e : SpellFx.effects()) {
                if (e instanceof Mark mark && mark.player == player) {
                    mark.kill();
                }
            }
        }

        @Override
        public float life() {
            return this.at == null ? 0.0F : this.life;
        }

        @Override
        public void draw(Frame f) {
            if (this.at == null) {
                return;
            }
            float age = this.age();
            float in = (float)Math.min(1.0, age / 10.0);
            float pulse = 0.7F + 0.3F * (float)Math.sin(age * 0.15);
            Vec3 base = this.at.add(0.0, 0.06, 0.0);
            f.line(this.at, this.at.add(0.0, 40.0 * easeOut(age / 12.0), 0.0), 0.45F, 0.55F * in * pulse, ABYSS);
            f.line(this.at, this.at.add(0.0, 40.0 * easeOut(age / 12.0), 0.0), 0.1F, 0.8F * in, 0xFFFFFF);
            double turn = age * 0.03;
            this.circle.draw(f);
            for (int i = 0; i < 4; i++) {
                double a = turn + i * Math.PI / 2.0;
                Vec3 d = new Vec3(Math.cos(a), 0.0, Math.sin(a));
                f.line(base.add(d.scale(0.95)), base.add(d.scale(1.55)), 0.08F, 0.8F * in, ABYSS);
            }
            Vec3 crystal = this.at.add(0.0, 2.2 + Math.sin(age * 0.08) * 0.15, 0.0);
            f.flare(crystal, 1.6F * pulse, ABYSS, in);
            f.flare(crystal, 0.6F, 0xFFFFFF, in);
        }

        @Override
        public void tick(ClientLevel level) {
            if (this.at == null || level.getRandom().nextInt(4) != 0) {
                return;
            }
            RandomSource random = level.getRandom();
            double a = random.nextDouble() * Math.PI * 2.0;
            emit(level, sparkle(ABYSS, 0.12F, 40).gravity(-0.004F), this.at.add(Math.cos(a) * 1.2, 0.1, Math.sin(a) * 1.2), new Vec3(0.0, 0.04, 0.0));
        }
    }
}
