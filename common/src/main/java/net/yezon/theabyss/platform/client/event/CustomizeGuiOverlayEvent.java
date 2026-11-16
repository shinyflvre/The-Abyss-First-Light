package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class CustomizeGuiOverlayEvent extends Event {

    public static class BossEventProgress extends CustomizeGuiOverlayEvent implements ICancellableEvent {
        private final net.minecraft.client.gui.components.LerpingBossEvent bossEvent;
        private final net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics;
        private final int x;
        private final int y;

        public BossEventProgress(net.minecraft.client.gui.components.LerpingBossEvent bossEvent, net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, int x, int y) {
            this.bossEvent = bossEvent;
            this.guiGraphics = guiGraphics;
            this.x = x;
            this.y = y;
        }

        public net.minecraft.client.gui.components.LerpingBossEvent getBossEvent() {
            return this.bossEvent;
        }

        public net.minecraft.client.gui.GuiGraphicsExtractor getGuiGraphics() {
            return this.guiGraphics;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
