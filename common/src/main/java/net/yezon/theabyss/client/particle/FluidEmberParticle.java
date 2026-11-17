package net.yezon.theabyss.client.particle;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class FluidEmberParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final ParticleOptions smoke;

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, Supplier<? extends ParticleOptions> smoke) {
        return (type, level, x, y, z, vx, vy, vz) -> new FluidEmberParticle(level, x, y, z, sprites, smoke.get());
    }

    protected FluidEmberParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, ParticleOptions smoke) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.setSprite(sprites.get(0, 1));
        this.sprites = sprites;
        this.smoke = smoke;
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.xd *= 0.8F;
        this.yd *= 0.8F;
        this.zd *= 0.8F;
        this.yd = this.random.nextFloat() * 0.4F + 0.05F;
        this.quadSize = this.quadSize * (this.random.nextFloat() * 2.0F + 0.2F);
        this.lifetime = (int)(16.0 / (this.random.nextFloat() * 0.8 + 0.2));
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float partialTick) {
        int light = super.getLightColor(partialTick);
        return light & 0xFF0000 | 15 << 4;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSpriteFromAge(this.sprites);
        float odds = (float)this.age / this.lifetime;
        if (this.random.nextFloat() > odds) {
            this.level.addParticle(this.smoke, this.x, this.y, this.z, this.xd, this.yd, this.zd);
        }
    }
}
