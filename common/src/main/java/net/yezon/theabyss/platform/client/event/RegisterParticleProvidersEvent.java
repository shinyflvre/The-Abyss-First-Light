package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterParticleProvidersEvent extends Event {
    public abstract <T extends net.minecraft.core.particles.ParticleOptions> void registerSpriteSet(net.minecraft.core.particles.ParticleType<T> a0, SpriteParticleRegistration<T> a1);

    @FunctionalInterface
    public interface SpriteParticleRegistration<T extends net.minecraft.core.particles.ParticleOptions> {
        net.minecraft.client.particle.ParticleProvider<T> create(net.minecraft.client.particle.SpriteSet sprites);
    }
}
