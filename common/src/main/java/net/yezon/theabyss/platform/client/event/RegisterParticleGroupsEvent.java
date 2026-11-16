package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterParticleGroupsEvent extends Event {
    public abstract void register(net.minecraft.client.particle.ParticleRenderType a0, java.util.function.Function<net.minecraft.client.particle.ParticleEngine, net.minecraft.client.particle.ParticleGroup<?>> a1);
}
