package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterParticleProvidersEvent extends Event {
    public abstract <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration);

    public abstract <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider);

    @FunctionalInterface
    public interface SpriteParticleRegistration<T extends ParticleOptions> {
        ParticleProvider<T> create(SpriteSet sprites);
    }
}
