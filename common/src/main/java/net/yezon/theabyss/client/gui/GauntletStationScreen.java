package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.world.inventory.GauntletStationMenu;

public class GauntletStationScreen extends AbstractContainerScreen<GauntletStationMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/gauntlet_station_gui.png");

    public GauntletStationScreen(GauntletStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GauntletStationMenu.WIDTH;
        this.imageHeight = GauntletStationMenu.HEIGHT;
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
        GuiGraphicsCompat.text(graphics, this.font, this.title, 8, 6, 0xFFB4FDEA, false);
    }
}
