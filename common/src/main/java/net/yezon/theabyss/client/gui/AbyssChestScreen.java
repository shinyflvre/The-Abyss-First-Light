package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.yezon.theabyss.compat.GuiGraphicsCompat;

public class AbyssChestScreen extends AbstractContainerScreen<ChestMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/abyss_chest.png");
    private static final int LABEL = -6684724;
    private final int rows;

    public AbyssChestScreen(ChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + menu.getRowCount() * 18;
        this.rows = menu.getRowCount();
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int top = this.rows * 18 + 17;
        GuiGraphicsCompat.blit(graphics, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, top, 256, 256);
        GuiGraphicsCompat.blit(graphics, BACKGROUND, this.leftPos, this.topPos + top, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiGraphicsCompat.text(graphics, this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL, false);
        GuiGraphicsCompat.text(graphics, this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL, false);
    }
}
