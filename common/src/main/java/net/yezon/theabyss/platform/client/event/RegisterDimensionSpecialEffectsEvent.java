package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterDimensionSpecialEffectsEvent extends Event {
    public abstract void register(ResourceLocation effectsId, DimensionSpecialEffects effects);
}
