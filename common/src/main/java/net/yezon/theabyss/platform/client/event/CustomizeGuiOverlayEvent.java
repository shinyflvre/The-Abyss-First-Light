package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class CustomizeGuiOverlayEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    protected CustomizeGuiOverlayEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public static class BossEventProgress extends CustomizeGuiOverlayEvent implements ICancellableEvent {
        private final LerpingBossEvent bossEvent;
        private final int x;
        private final int y;
        private int increment;

        public BossEventProgress(GuiGraphics guiGraphics, DeltaTracker partialTick, LerpingBossEvent bossEvent, int x, int y, int increment) {
            super(guiGraphics, partialTick);
            this.bossEvent = bossEvent;
            this.x = x;
            this.y = y;
            this.increment = increment;
        }

        public LerpingBossEvent getBossEvent() {
            return this.bossEvent;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getIncrement() {
            return this.increment;
        }

        public void setIncrement(int value) {
            this.increment = value;
        }
    }
}
