package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class ScreenEvent extends Event {

    public abstract static class Render extends ScreenEvent {

        public static class Post extends Render {
            private final net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics;
            private final net.minecraft.client.gui.screens.Screen screen;

            public Post(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, net.minecraft.client.gui.screens.Screen screen) {
                this.guiGraphics = guiGraphics;
                this.screen = screen;
            }

            public net.minecraft.client.gui.GuiGraphicsExtractor getGuiGraphics() {
                return this.guiGraphics;
            }

            public net.minecraft.client.gui.screens.Screen getScreen() {
                return this.screen;
            }
        }
    }
}
