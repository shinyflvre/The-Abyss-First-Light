package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.network.ResearchButtonMessage;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/research.png");
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/research_gui.png");

    public ResearchScreen(ResearchMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiGraphicsCompat.blit(graphics, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        GuiGraphicsCompat.blit(graphics, BACKGROUND, this.leftPos - 1, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiGraphicsCompat.text(graphics, this.font, Component.translatable("gui.theabyss.research.label_research_table"), 6, 7, -6684724, false);
        GuiGraphicsCompat.text(graphics, this.font, Component.translatable("gui.theabyss.research.label_cost"), 78, 25, -6684724, false);
        GuiGraphicsCompat.text(graphics, this.font, Component.translatable("gui.theabyss.research.label_5_xp_levels"), 105, 25, -16724788, false);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.theabyss.research.button_research"), button -> {
            ResearchMenu menu = this.menu;
            ClientPacketDistributor.sendToServer(new ResearchButtonMessage(0, menu.x, menu.y, menu.z));
            ResearchButtonMessage.handleButtonAction(menu.entity, 0, menu.x, menu.y, menu.z);
        }).bounds(this.leftPos + 78, this.topPos + 42, 65, 20).build());
    }
}
