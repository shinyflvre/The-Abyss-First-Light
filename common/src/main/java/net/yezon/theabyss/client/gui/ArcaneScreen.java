package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.world.inventory.ArcaneMenu;

public class ArcaneScreen extends AbstractContainerScreen<ArcaneMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/arcane_gui.png");

    public ArcaneScreen(ArcaneMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, 176, 200);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("gui.theabyss.arcane.label_arcane"), 132, 6, -6684724, false);
        if (!this.menu.powered()) {
            Component hint = Component.translatable("gui.theabyss.arcane.needs_power");
            graphics.text(this.font, hint, (this.imageWidth - this.font.width(hint)) / 2, -12, 0xFFFF8A8A, true);
        }
    }
}
