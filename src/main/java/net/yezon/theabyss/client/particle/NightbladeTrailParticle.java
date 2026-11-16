package net.yezon.theabyss.client.particle;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class NightbladeTrailParticle extends SingleQuadParticle {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/nightblade_trail"))
        .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
        .withCull(false)
        .build();
    private static final SingleQuadParticle.Layer LAYER = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, PIPELINE, RenderPipelines.OIT_PARTICLE);
    public static final SingleQuadParticle.Layer GLOW_LAYER = LAYER;

    private final float startSize;

    public static TrailProvider provider(SpriteSet spriteSet) {
        return new TrailProvider(spriteSet);
    }

    protected NightbladeTrailParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, boolean red) {
        super(level, x, y, z, sprites.first());
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
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return LAYER;
    }

    public static class TrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public TrailProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random
        ) {
            return new NightbladeTrailParticle(level, x, y, z, this.spriteSet, vx > 0.5);
        }
    }
}
