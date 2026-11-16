package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;

final class FlameTrailFx extends Effect {
    private static final int FIRE = 0xFF5A1E;
    private static final int EMBER = 0xFFB040;
    private static final int HOT = 0xFFE6B0;

    private final Vec3[] path;
    private final int life;

    FlameTrailFx(SpellFxMessage m) {
        int n = m.data().size() / 3;
        this.path = new Vec3[n];
        for (int i = 0; i < n; i++) {
            this.path[i] = SpellFx.vec(m, i * 3).add(0.0, 0.06, 0.0);
        }
        this.life = m.ticks();
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void draw(Frame f) {
        if (this.path.length < 2) {
            return;
        }
        float age = this.age();
        float fade = this.fade(2.0F, 20.0F);
        float flicker = 0.75F + 0.25F * (float)Math.sin(age * 1.7) * (float)Math.cos(age * 0.9);
        f.groundBand(this.path, 1.6F, 0.9F * fade * flicker, FIRE);
        f.groundBand(this.path, 0.7F, fade * flicker, EMBER);
        f.groundBand(this.path, 0.22F, fade, HOT);
    }

    @Override
    public void tick(ClientLevel level) {
        if (this.path.length < 2 || this.age() > this.life - 12) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int k = 0; k < 4; k++) {
            int i = random.nextInt(this.path.length - 1);
            Vec3 at = this.path[i].lerp(this.path[i + 1], random.nextDouble()).add(random.nextGaussian() * 0.25, 0.0, random.nextGaussian() * 0.25);
            emit(level, flame(k % 2 == 0 ? FIRE : EMBER, 0.38F + random.nextFloat() * 0.2F, 12).gravity(-0.025F), at, new Vec3(0.0, 0.03, 0.0));
            if (k == 0) {
                emit(level, sparkle(EMBER, 0.11F, 26).gravity(-0.01F).flags(FxOptions.FLICKER | FxOptions.HOT), at, new Vec3(0.0, 0.05, 0.0));
            }
        }
        if (random.nextInt(4) == 0) {
            int i = random.nextInt(this.path.length);
            emit(level, smoke(0x2E2220, 0.7F, 36).gravity(-0.006F), this.path[i].add(0.0, 0.6, 0.0), new Vec3(0.0, 0.03, 0.0));
        }
    }
}
