package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SpursFXParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public static SpursFXParticle.SpursFXParticleProvider provider(SpriteSet spriteSet) {
        return new SpursFXParticle.SpursFXParticleProvider(spriteSet);
    }

    protected SpursFXParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.setSprite(spriteSet.get(0, 1));
        this.spriteSet = spriteSet;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= 0.2F;
        this.lifetime = 35;
        this.gravity = -0.01F;
        this.hasPhysics = true;
        this.xd = vx * 0.002;
        this.yd = vy * 0.002;
        this.zd = vz * 0.002;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSprite(this.spriteSet.get(this.age / 6 % 1 + 1, 1));
        }
    }

    public static class SpursFXParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public SpursFXParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SpursFXParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
