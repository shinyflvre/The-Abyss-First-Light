package net.yezon.theabyss.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.ScreenFx;

public class AmbientSmokeParticle extends SingleQuadParticle {
    public static final com.mojang.renderpearl.api.pipeline.RenderPipeline PIPELINE = com.mojang.renderpearl.api.pipeline.RenderPipeline
        .builder(net.yezon.theabyss.mixin.accessor.client.RenderPipelinesAccessor.theabyss$particleSnippet())
        .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("theabyss", "pipeline/ambient_smoke"))
        .withColorTargetState(new com.mojang.renderpearl.api.pipeline.ColorTargetState(com.mojang.renderpearl.api.pipeline.BlendFunction.TRANSLUCENT))
        .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(com.mojang.renderpearl.api.pipeline.CompareOp.GREATER_THAN_OR_EQUAL, false))
        .withCull(false)
        .build();
    private static final SingleQuadParticle.Layer LAYER = new SingleQuadParticle.Layer(
        true, net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES, PIPELINE, net.minecraft.client.renderer.RenderPipelines.OIT_PARTICLE);

    private static final java.util.List<AmbientSmokeParticle> LIVE = new java.util.ArrayList<>();
    private long lastTick;

    private final float peakAlpha;
    private static final float FAR_FADE = 26.0F;
    private static final float FAR_LIMIT = 32.0F;

    public static AmbientSmokeParticleProvider provider(SpriteSet spriteSet) {
        return new AmbientSmokeParticleProvider(spriteSet);
    }

    public static int alive(ClientLevel current) {
        long now = current.getGameTime();
        LIVE.removeIf(puff -> !puff.isAlive() || puff.level != current || Math.abs(now - puff.lastTick) > 5L);
        return LIVE.size();
    }

    protected AmbientSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setSprite(sprites.get(this.random));
        this.lifetime = 240 + this.random.nextInt(200);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.quadSize = 1.8F + this.random.nextFloat() * 1.8F;
        this.peakAlpha = 0.272F + this.random.nextFloat() * 0.152F;
        this.alpha = 0.0F;
        this.updateColour();
        this.lastTick = level.getGameTime();
        LIVE.add(this);
    }

    @Override
    public void tick() {
        this.lastTick = this.level.getGameTime();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.updateColour();
        float fadeIn = Math.min(1.0F, this.age / 30.0F);
        float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 60.0F);
        Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        float distance = (float)camera.distanceTo(new Vec3(this.x, this.y, this.z));
        if (distance > FAR_LIMIT) {
            this.remove();
            return;
        }
        float far = Mth.clamp((FAR_LIMIT - distance) / (FAR_LIMIT - FAR_FADE), 0.0F, 1.0F);
        float near = Mth.clamp((distance - 1.5F) / 3.0F, 0.0F, 1.0F);
        this.alpha = this.peakAlpha * fadeIn * fadeOut * near * far;
    }

    private void updateColour() {
        int fog = ScreenFx.fogColour();
        this.setColor(lift(ARGB.red(fog)), lift(ARGB.green(fog)), lift(ARGB.blue(fog)));
    }

    private static float lift(int channel) {
        return Math.min(1.0F, channel / 255.0F * 2.0F + 0.06F);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return LAYER;
    }

    public static class AmbientSmokeParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public AmbientSmokeParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random
        ) {
            return new AmbientSmokeParticle(level, x, y, z, vx, vy, vz, this.spriteSet);
        }
    }
}
