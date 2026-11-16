package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class CrystalDropParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    protected CrystalDropParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.quadSize *= 2.5F;
        this.lifetime = 5 + this.random.nextInt(11);
        this.gravity = 1.0F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSpriteFromAge(this.sprites);
        if (this.onGround) {
            this.level.addParticle(TheabyssModParticleTypes.CRYSTAL_GROUND.get(), this.x, this.y + 0.01, this.z, 0.0, 0.0, 0.0);
            this.remove();
        }
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CrystalDropParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
