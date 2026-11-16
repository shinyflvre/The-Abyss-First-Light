package net.yezon.theabyss.platform.client.renderstate;

import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterRenderStateModifiersEvent extends Event {
    public abstract <E extends Entity, S extends EntityRenderState> void registerEntityModifier(
        Class<? extends EntityRenderer<? extends E, ? extends S>> baseRenderer, BiConsumer<E, S> modifier);
}
