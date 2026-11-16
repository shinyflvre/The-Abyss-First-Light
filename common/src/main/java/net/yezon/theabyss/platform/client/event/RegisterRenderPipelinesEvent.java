package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterRenderPipelinesEvent extends Event {
    public abstract void registerPipeline(com.mojang.renderpearl.api.pipeline.RenderPipeline a0);
}
