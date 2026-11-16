package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.network.ResearchButtonMessage;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/research.png");
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/research_gui.png");

    public ResearchScreen(ResearchMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos - 1, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("gui.theabyss.research.label_research_table"), 6, 7, -6684724, false);
        graphics.text(this.font, Component.translatable("gui.theabyss.research.label_cost"), 78, 25, -6684724, false);
        graphics.text(this.font, Component.translatable("gui.theabyss.research.label_5_xp_levels"), 105, 25, -16724788, false);
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
