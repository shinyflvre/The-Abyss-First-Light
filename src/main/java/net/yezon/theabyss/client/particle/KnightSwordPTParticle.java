package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class KnightSwordPTParticle extends SingleQuadParticle {
    private final SpriteSet spriteSet;

    public static KnightSwordPTParticle.KnightSwordPTParticleProvider provider(SpriteSet spriteSet) {
        return new KnightSwordPTParticle.KnightSwordPTParticleProvider(spriteSet);
    }

    protected KnightSwordPTParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z, spriteSet.first());
        this.spriteSet = spriteSet;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= 0.2F;
        this.lifetime = 20;
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
            this.setSprite(this.spriteSet.get(this.age / 10 % 1 + 1, 1));
        }
    }

    public static class KnightSwordPTParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public KnightSwordPTParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random
        ) {
            return new KnightSwordPTParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
