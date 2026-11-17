package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FireFXParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private float angularVelocity;
    private float angularAcceleration;

    public static FireFXParticle.FireFXParticleProvider provider(SpriteSet spriteSet) {
        return new FireFXParticle.FireFXParticleProvider(spriteSet);
    }

    protected FireFXParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.setSprite(spriteSet.get(0, 1));
        this.spriteSet = spriteSet;
        this.setSize(0.0F, 0.0F);
        this.quadSize *= 2.5F;
        this.lifetime = Math.max(1, 20 + (this.random.nextInt(80) - 40));
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.xd = vx * 0.2;
        this.yd = vy * 0.2;
        this.zd = vz * 0.2;
        this.angularVelocity = 0.0F;
        this.angularAcceleration = 0.05F;
        this.setSpriteFromAge(spriteSet);
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
        this.oRoll = this.roll;
        this.roll = this.roll + this.angularVelocity;
        this.angularVelocity = this.angularVelocity + this.angularAcceleration;
        if (!this.removed) {
            this.setSprite(this.spriteSet.get(this.age / 1 % 8 + 1, 8));
        }
    }

    public static class FireFXParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public FireFXParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FireFXParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
