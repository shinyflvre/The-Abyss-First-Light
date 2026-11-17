package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class NightbladeTrailParticle extends TextureSheetParticle {
    public static final ParticleRenderType GLOW_TYPE = AbyssParticleRenderTypes.GLOW;

    private final float startSize;

    public static TrailProvider provider(SpriteSet spriteSet) {
        return new TrailProvider(spriteSet);
    }

    protected NightbladeTrailParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, boolean red) {
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
        this.lifetime = 9 + this.random.nextInt(4);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.startSize = 0.32F + this.random.nextFloat() * 0.08F;
        this.quadSize = this.startSize;
        if (red) {
            this.setColor(1.0F, 0.349F, 0.349F);
        } else {
            this.setColor(0.32F, 0.82F, 1.0F);
        }
        this.alpha = 0.9F;
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
        float life = 1.0F - this.age / (float)this.lifetime;
        this.quadSize = this.startSize * (0.4F + 0.6F * life);
        this.alpha = 0.9F * life * life;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GLOW_TYPE;
    }

    public static class TrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public TrailProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new NightbladeTrailParticle(level, x, y, z, this.spriteSet, vx > 0.5);
        }
    }
}
