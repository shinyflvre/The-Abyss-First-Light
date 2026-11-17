package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.world.inventory.MortarMenu;

public class MortarScreen extends AbstractContainerScreen<MortarMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/morta_gui.png");

    public MortarScreen(MortarMenu container, Inventory inventory, Component text) {
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
        GuiGraphicsCompat.blit(graphics, BACKGROUND, this.leftPos - 1, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiGraphicsCompat.text(graphics, this.font, Component.translatable("gui.theabyss.mortar.label_mortar"), 132, 6, -6684724, false);
    }
}
