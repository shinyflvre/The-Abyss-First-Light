package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RenderFrameEvent extends Event {

    public static class Pre extends RenderFrameEvent {
        private final net.minecraft.client.DeltaTracker partialTick;

        public Pre(net.minecraft.client.DeltaTracker partialTick) {
            this.partialTick = partialTick;
        }

        public net.minecraft.client.DeltaTracker getPartialTick() {
            return this.partialTick;
        }
    }
}
