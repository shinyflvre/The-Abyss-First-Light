package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.world.inventory.ArcaneMenu;

public class ArcaneScreen extends AbstractContainerScreen<ArcaneMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/arcane_gui.png");

    public ArcaneScreen(ArcaneMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 200;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiGraphicsCompat.blit(graphics, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiGraphicsCompat.text(graphics, this.font, Component.translatable("gui.theabyss.arcane.label_arcane"), 132, 6, -6684724, false);
        if (!this.menu.powered()) {
            Component hint = Component.translatable("gui.theabyss.arcane.needs_power");
            GuiGraphicsCompat.text(graphics, this.font, hint, (this.imageWidth - this.font.width(hint)) / 2, -12, 0xFFFF8A8A, true);
        }
    }
}
