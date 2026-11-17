package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.yezon.theabyss.gravity.GravityMoteOptions;

public class GravityMoteParticle extends TextureSheetParticle {
    private static final float[] FAR = {0.69F, 0.34F, 0.87F};
    private static final float[] NEAR = {1.0F, 0.80F, 0.97F};
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double pathX;
    private final double pathY;
    private final double pathZ;
    private final float startSize;

    protected GravityMoteParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.pathX = dx;
        this.pathY = dy;
        this.pathZ = dz;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.lifetime = 18 + (int)(distance * 9.0) + this.random.nextInt(8);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.startSize = 0.0225F + this.random.nextFloat() * 0.0175F;
        this.quadSize = this.startSize;
        this.setColor(FAR[0], FAR[1], FAR[2]);
        this.alpha = 0.0F;
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
        float t = this.age / (float)this.lifetime;
        double pull = t * t * (0.35 + 0.65 * t);
        this.setPos(this.startX + this.pathX * pull, this.startY + this.pathY * pull, this.startZ + this.pathZ * pull);
        float near = t * t;
        this.setColor(FAR[0] + (NEAR[0] - FAR[0]) * near, FAR[1] + (NEAR[1] - FAR[1]) * near, FAR[2] + (NEAR[2] - FAR[2]) * near);
        float fadeIn = Math.min(1.0F, t * 6.0F);
        float fadeOut = t > 0.9F ? (1.0F - t) * 10.0F : 1.0F;
        this.alpha = 0.85F * fadeIn * fadeOut;
        this.quadSize = this.startSize * (0.7F + 0.6F * t) * (t > 0.9F ? 0.5F + 5.0F * (1.0F - t) : 1.0F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return NightbladeTrailParticle.GLOW_TYPE;
    }

    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    public static class Provider implements ParticleProvider<GravityMoteOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(GravityMoteOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new GravityMoteParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
