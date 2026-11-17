package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.yezon.theabyss.platform.event.Event;

public abstract class ScreenEvent extends Event {
    private final Screen screen;

    protected ScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public abstract static class Render extends ScreenEvent {
        private final GuiGraphics guiGraphics;
        private final int mouseX;
        private final int mouseY;
        private final float partialTick;

        protected Render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super(screen);
            this.guiGraphics = guiGraphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.partialTick = partialTick;
        }

        public GuiGraphics getGuiGraphics() {
            return this.guiGraphics;
        }

        public int getMouseX() {
            return this.mouseX;
        }

        public int getMouseY() {
            return this.mouseY;
        }

        public float getPartialTick() {
            return this.partialTick;
        }

        public static class Post extends Render {
            public Post(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super(screen, guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }
}
