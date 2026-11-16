package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.world.inventory.GauntletStationMenu;

public class GauntletStationScreen extends AbstractContainerScreen<GauntletStationMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/gauntlet_station_gui.png");

    public GauntletStationScreen(GauntletStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GauntletStationMenu.WIDTH, GauntletStationMenu.HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 6, 0xFFB4FDEA, false);
    }
}
