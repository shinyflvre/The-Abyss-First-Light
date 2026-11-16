package net.yezon.theabyss.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.yezon.theabyss.init.TheabyssModKeyMappings;

public class DialogueScreen extends Screen {
    public DialogueScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        DialogueClient.hover(mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            DialogueClient.click(event.x(), event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        DialogueClient.scroll(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_4) {
            DialogueClient.keyChoice(key - InputConstants.KEY_1);
            return true;
        }
        for (int i = 0; i < 4; i++) {
            if (Minecraft.getInstance().options.keyHotbarSlots[i].matches(event)) {
                DialogueClient.keyChoice(i);
                return true;
            }
        }
        if (TheabyssModKeyMappings.DIALOG_CONTINUE.matches(event) || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_SPACE) {
            DialogueClient.continueKey();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        DialogueClient.cancel();
        super.onClose();
    }
}
