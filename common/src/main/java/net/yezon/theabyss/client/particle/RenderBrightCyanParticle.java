package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class RenderBrightCyanParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public static RenderBrightCyanParticle.RenderBrightCyanParticleProvider provider(SpriteSet spriteSet) {
        return new RenderBrightCyanParticle.RenderBrightCyanParticleProvider(spriteSet);
    }

    protected RenderBrightCyanParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.setSprite(spriteSet.get(0, 1));
        this.spriteSet = spriteSet;
        this.setSize(0.2F, 0.2F);
        this.quadSize *= 0.8F;
        this.lifetime = Math.max(1, 10 + (this.random.nextInt(30) - 15));
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.xd = vx * 1.0;
        this.yd = vy * 1.0;
        this.zd = vz * 1.0;
        this.setSprite(spriteSet.get(this.random));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static class RenderBrightCyanParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public RenderBrightCyanParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RenderBrightCyanParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
