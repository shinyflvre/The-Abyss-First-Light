package net.yezon.theabyss.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class CrystalGroundParticle extends SingleQuadParticle {
    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    private final float yaw;

    protected CrystalGroundParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.quadSize *= 2.5F;
        this.lifetime = 50 + this.random.nextInt(30);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.yaw = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    @Override
    public void tick() {
        super.tick();
        int left = this.lifetime - this.age;
        this.setAlpha(left < 20 ? left / 20.0F : 1.0F);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        this.extractRotatedQuad(state, camera, new Quaternionf().rotationY(this.yaw).rotateX((float)(-Math.PI / 2.0)), partialTick);
        this.extractRotatedQuad(state, camera, new Quaternionf().rotationY(this.yaw).rotateX((float)(Math.PI / 2.0)), partialTick);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CrystalGroundParticle(level, x, y, z, this.sprites);
        }
    }
}
