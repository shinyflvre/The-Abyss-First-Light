package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;

public class GlowParticle extends TextureSheetParticle {
    private final float startSize;
    private final int flags;
    private final float drag;

    protected GlowParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, GlowParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(0, 1));
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = Math.max(1, options.lifetime() + this.random.nextInt(Math.max(1, options.lifetime() / 5)) - options.lifetime() / 10);
        this.gravity = options.gravity();
        this.drag = options.drag();
        this.flags = options.flags();
        this.hasPhysics = false;
        this.startSize = options.size() * (0.85F + this.random.nextFloat() * 0.3F);
        this.quadSize = this.startSize;
        int c = options.color();
        this.setColor(ARGB.red(c) / 255.0F, ARGB.green(c) / 255.0F, ARGB.blue(c) / 255.0F);
        this.alpha = (this.flags & GlowParticleOptions.FADE_IN) != 0 ? 0.0F : 0.95F;
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
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.drag;
        this.yd *= this.drag;
        this.zd *= this.drag;
        float t = this.age / (float)this.lifetime;
        float life = 1.0F - t;
        float size = 1.0F;
        if ((this.flags & GlowParticleOptions.SHRINK) != 0) {
            size *= 0.35F + 0.65F * life;
        }
        if ((this.flags & GlowParticleOptions.GROW) != 0) {
            size *= 1.0F + 1.4F * t;
        }
        this.quadSize = this.startSize * size;
        float alpha = 0.95F * life * (2.0F - life);
        if ((this.flags & GlowParticleOptions.FADE_IN) != 0) {
            alpha *= Math.min(1.0F, t * 5.0F);
        }
        if ((this.flags & GlowParticleOptions.FLICKER) != 0) {
            alpha *= 0.45F + 0.55F * this.random.nextFloat();
        }
        this.alpha = alpha;
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

    public static class Provider implements ParticleProvider<GlowParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new GlowParticle(level, x, y, z, vx, vy, vz, options, this.sprites);
        }
    }
}
