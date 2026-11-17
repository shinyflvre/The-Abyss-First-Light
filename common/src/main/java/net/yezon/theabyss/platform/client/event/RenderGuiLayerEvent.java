package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderGuiLayerEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;
    private final ResourceLocation name;

    protected RenderGuiLayerEvent(GuiGraphics guiGraphics, DeltaTracker partialTick, ResourceLocation name) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
        this.name = name;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public static class Pre extends RenderGuiLayerEvent implements ICancellableEvent {
        public Pre(GuiGraphics guiGraphics, DeltaTracker partialTick, ResourceLocation name) {
            super(guiGraphics, partialTick, name);
        }
    }

    public static class Post extends RenderGuiLayerEvent {
        public Post(GuiGraphics guiGraphics, DeltaTracker partialTick, ResourceLocation name) {
            super(guiGraphics, partialTick, name);
        }
    }
}
