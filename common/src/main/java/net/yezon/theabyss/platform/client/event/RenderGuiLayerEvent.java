package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderGuiLayerEvent extends Event {

    public static class Post extends RenderGuiLayerEvent {
        private final net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics;
        private final net.minecraft.resources.Identifier name;

        public Post(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, net.minecraft.resources.Identifier name) {
            this.guiGraphics = guiGraphics;
            this.name = name;
        }

        public net.minecraft.client.gui.GuiGraphicsExtractor getGuiGraphics() {
            return this.guiGraphics;
        }

        public net.minecraft.resources.Identifier getName() {
            return this.name;
        }
    }

    public static class Pre extends RenderGuiLayerEvent implements ICancellableEvent {
        private final net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics;
        private final net.minecraft.resources.Identifier name;

        public Pre(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, net.minecraft.resources.Identifier name) {
            this.guiGraphics = guiGraphics;
            this.name = name;
        }

        public net.minecraft.client.gui.GuiGraphicsExtractor getGuiGraphics() {
            return this.guiGraphics;
        }

        public net.minecraft.resources.Identifier getName() {
            return this.name;
        }
    }
}
