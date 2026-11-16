package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ShockFXParticle extends SingleQuadParticle {
    private final SpriteSet spriteSet;

    public static ShockFXParticle.ShockFXParticleProvider provider(SpriteSet spriteSet) {
        return new ShockFXParticle.ShockFXParticleProvider(spriteSet);
    }

    protected ShockFXParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z, spriteSet.first());
        this.spriteSet = spriteSet;
        this.setSize(0.0F, 0.0F);
        this.quadSize *= 2.5F;
        this.lifetime = Math.max(1, 20 + (this.random.nextInt(80) - 40));
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.xd = vx * 0.2;
        this.yd = vy * 0.2;
        this.zd = vz * 0.2;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSprite(this.spriteSet.get(this.age / 1 % 7 + 1, 7));
        }
    }

    public static class ShockFXParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public ShockFXParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random
        ) {
            return new ShockFXParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
