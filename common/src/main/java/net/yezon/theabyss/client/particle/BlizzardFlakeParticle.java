package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlizzardFlakeParticle extends SingleQuadParticle {
    private final float phase;
    private final float flutter;

    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    protected BlizzardFlakeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setSprite(sprites.get(this.random));
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = 60 + this.random.nextInt(50);
        this.quadSize = 0.05F + this.random.nextFloat() * 0.07F;
        this.phase = this.random.nextFloat() * Mth.TWO_PI;
        this.flutter = 0.01F + this.random.nextFloat() * 0.025F;
        float shade = 0.9F + this.random.nextFloat() * 0.1F;
        this.setColor(shade * 0.93F, shade * 0.97F, shade);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float wobble = Mth.sin(this.age * 0.35F + this.phase);
        this.x += this.xd + wobble * this.flutter;
        this.y += this.yd;
        this.z += this.zd + Mth.cos(this.age * 0.3F + this.phase) * this.flutter;
        this.setPos(this.x, this.y, this.z);
        if (this.age % 4 == 0 && this.y < this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(this.x), Mth.floor(this.z))) {
            this.remove();
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
            RandomSource random) {
            return new BlizzardFlakeParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
