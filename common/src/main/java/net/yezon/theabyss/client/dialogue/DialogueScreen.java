package net.yezon.theabyss.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        DialogueClient.hover(mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            DialogueClient.click(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        DialogueClient.scroll(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_4) {
            DialogueClient.keyChoice(key - InputConstants.KEY_1);
            return true;
        }
        for (int i = 0; i < 4; i++) {
            if (Minecraft.getInstance().options.keyHotbarSlots[i].matches(key, scanCode)) {
                DialogueClient.keyChoice(i);
                return true;
            }
        }
        if (TheabyssModKeyMappings.DIALOG_CONTINUE.matches(key, scanCode) || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_SPACE) {
            DialogueClient.continueKey();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        DialogueClient.cancel();
        super.onClose();
    }
}
