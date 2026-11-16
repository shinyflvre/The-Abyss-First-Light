package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.world.inventory.MortarMenu;

public class MortarScreen extends AbstractContainerScreen<MortarMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/morta_gui.png");

    public MortarScreen(MortarMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, 176, 200);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos - 1, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("gui.theabyss.mortar.label_mortar"), 132, 6, -6684724, false);
    }
}
