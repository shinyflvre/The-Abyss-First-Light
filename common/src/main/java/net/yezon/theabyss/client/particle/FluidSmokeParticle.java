package net.yezon.theabyss.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class FluidSmokeParticle extends BaseAshSmokeParticle {
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, int dark, int light) {
        return (type, level, x, y, z, vx, vy, vz) -> new FluidSmokeParticle(level, x, y, z, vx, vy, vz, sprites, dark, light);
    }

    protected FluidSmokeParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites, int dark, int light) {
        super(level, x, y, z, 0.1F, 0.1F, 0.1F, xa, ya, za, 1.0F, sprites, 0.0F, 8, -0.1F, true);
        float t = this.random.nextFloat();
        this.setColor(mix(dark >> 16, light >> 16, t), mix(dark >> 8, light >> 8, t), mix(dark, light, t));
    }

    private static float mix(int a, int b, float t) {
        float from = (a & 255) / 255.0F;
        float to = (b & 255) / 255.0F;
        return from + (to - from) * t;
    }
}
