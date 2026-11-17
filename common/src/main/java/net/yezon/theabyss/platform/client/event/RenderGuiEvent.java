package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderGuiEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    protected RenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public static class Post extends RenderGuiEvent {
        public Post(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }

    public static class Pre extends RenderGuiEvent implements ICancellableEvent {
        public Pre(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }
}
