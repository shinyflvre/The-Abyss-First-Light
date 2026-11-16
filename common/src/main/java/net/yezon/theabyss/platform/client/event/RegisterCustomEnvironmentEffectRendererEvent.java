package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterCustomEnvironmentEffectRendererEvent extends Event {
    public abstract void registerSkyboxRenderer(net.minecraft.resources.Identifier a0, net.yezon.theabyss.platform.client.CustomSkyboxRenderer a1);
}
