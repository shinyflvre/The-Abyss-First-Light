package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterSpecialModelRendererEvent extends Event {
    public abstract void register(net.minecraft.resources.Identifier a0, com.mojang.serialization.MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<?>> a1);
}
