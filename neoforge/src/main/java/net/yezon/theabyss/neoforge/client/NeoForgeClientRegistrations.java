package net.yezon.theabyss.neoforge.client;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.platform.client.event.RegisterParticleProvidersEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class NeoForgeClientRegistrations {
    private NeoForgeClientRegistrations() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent.class,
            e -> AbyssEventBus.post(new RegisterParticleProvidersEvent() {
                @Override
                public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
                    e.registerSpriteSet(type, registration::create);
                }

                @Override
                public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
                    e.registerSpecial(type, provider);
                }
            }));
        NeoForgeClientEventBridge.register(bus);
    }
}
