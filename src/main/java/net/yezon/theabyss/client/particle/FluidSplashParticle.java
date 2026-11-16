package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;

public class FluidSplashParticle extends SingleQuadParticle {
    private static final int[] FRAME_END = {3, 6, 8};
    private final SpriteSet sprites;

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return (type, level, x, y, z, vx, vy, vz, random) -> new FluidSplashParticle(level, x, y, z, sprites);
    }

    protected FluidSplashParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.quadSize = 0.12F + this.random.nextFloat() * 0.06F;
        this.setPos(x, y + this.quadSize, z);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = FRAME_END[FRAME_END.length - 1];
        this.setSprite(sprites.get(0, FRAME_END.length - 1));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return LightCoordsUtil.withBlock(super.getLightCoords(partialTick), 15);
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
        int frame = 0;
        while (frame < FRAME_END.length - 1 && this.age >= FRAME_END[frame]) {
            frame++;
        }
        this.setSprite(this.sprites.get(frame, FRAME_END.length - 1));
    }
}
