package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class SoulFx extends Effect {
    private static final int PEARL = 0xE8E0FF;
    private static final int LILAC = 0xB49CFF;

    private final Vec3 from;
    private final Vec3 to;
    private final int life;
    private boolean burst;

    SoulFx(SpellFxMessage m) {
        this.from = SpellFx.vec(m, 0).add(0.0, 1.0, 0.0);
        this.to = SpellFx.vec(m, 3).add(0.0, 1.0, 0.0);
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        float k = this.fade(0.0F, this.life * 0.8F);
        f.line(this.from, this.to, 0.7F * k, 0.6F * k, LILAC);
        f.line(this.from, this.to, 0.15F * k, k, 0xFFFFFF);
        Vec3 side = this.to.subtract(this.from).cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        double open = easeOut(age / 5.0);
        for (Vec3 at : new Vec3[] {this.from, this.to}) {
            f.circle(at, new Vec3(0.0, 1.0, 0.0), side, 1.1 * open, 0.12F * k, k, PEARL);
        }
        f.flare(this.from.lerp(this.to, 0.5), 2.0F * k, PEARL, 0.7F * k);
    }

    @Override
    public void tick(ClientLevel level) {
        if (this.burst) {
            return;
        }
        this.burst = true;
        RandomSource random = level.getRandom();
        for (int k = 0; k < 24; k++) {
            Vec3 at = this.from.lerp(this.to, random.nextDouble()).add(random.nextGaussian() * 0.2, random.nextGaussian() * 0.4, random.nextGaussian() * 0.2);
            emit(level, sparkle(k % 2 == 0 ? PEARL : LILAC, 0.12F, 26).gravity(-0.003F), at, new Vec3(0.0, 0.02, 0.0));
        }
    }
}
