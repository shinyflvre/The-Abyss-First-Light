package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.block.entity.StationMachineBlockEntity;
import net.yezon.theabyss.world.inventory.MachineMenu;

public class MachineScreen<T extends MachineMenu> extends AbstractContainerScreen<T> {
    private static final int[][] PATH = {{43, 55}, {82, 55}, {82, 42}, {95, 42}, {95, 55}, {132, 55}};
    private static final int BAR_X = 100;
    private static final int BAR_Y = 27;
    private static final int BAR_H = 16;
    private final Identifier background;
    private final Component label;
    private final int labelColour;

    public MachineScreen(T menu, Inventory inventory, Component title, Identifier background, Component label, int labelColour) {
        super(menu, inventory, title, 176, 166);
        this.background = background;
        this.label = label;
        this.labelColour = labelColour;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x0 = this.leftPos - 1;
        int y0 = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, x0, y0, 0.0F, 0.0F, 256, 256, 256, 256);
        this.progressLine(graphics, x0, y0, this.menu.progress());
        int bx = this.leftPos + BAR_X;
        int by = this.topPos + BAR_Y;
        graphics.fill(bx, by, bx + 3, by + BAR_H, 0xFF0A2A2E);
        int h = Math.round(this.menu.energy() * BAR_H);
        if (h > 0) {
            graphics.fill(bx, by + BAR_H - h, bx + 3, by + BAR_H, 0xFFB4FDEA);
        }
    }

    private void progressLine(GuiGraphicsExtractor graphics, int x0, int y0, float progress) {
        if (progress <= 0.0F) {
            return;
        }
        int total = 0;
        for (int i = 0; i + 1 < PATH.length; i++) {
            total += Math.abs(PATH[i + 1][0] - PATH[i][0]) + Math.abs(PATH[i + 1][1] - PATH[i][1]);
        }
        int left = Math.round(progress * total);
        int headX = PATH[0][0];
        int headY = PATH[0][1];
        for (int i = 0; i + 1 < PATH.length && left > 0; i++) {
            int ax = PATH[i][0];
            int ay = PATH[i][1];
            int bx = PATH[i + 1][0];
            int by = PATH[i + 1][1];
            int length = Math.abs(bx - ax) + Math.abs(by - ay);
            int step = Math.min(left, length);
            int ex = ax + Integer.signum(bx - ax) * step;
            int ey = ay + Integer.signum(by - ay) * step;
            segment(graphics, x0 + Math.min(ax, ex), y0 + Math.min(ay, ey), x0 + Math.max(ax, ex), y0 + Math.max(ay, ey));
            left -= step;
            headX = ex;
            headY = ey;
        }
        graphics.fill(x0 + headX - 1, y0 + headY - 1, x0 + headX + 2, y0 + headY + 2, 0xFFFFFFFF);
    }

    private static void segment(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2) {
        graphics.fill(x1 - 1, y1 - 1, x2 + 2, y2 + 2, 0xFF6FF3FF);
        graphics.fill(x1, y1, x2 + 1, y2 + 1, 0xFFFFFFFF);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.label, 6, 7, this.labelColour, false);
        String key = switch (this.menu.state()) {
            case StationMachineBlockEntity.NEEDS_RESEARCH -> "gui.theabyss.station.needs_research";
            case StationMachineBlockEntity.NO_ENERGY -> "gui.theabyss.station.no_energy";
            case StationMachineBlockEntity.OUTPUT_FULL -> "gui.theabyss.station.output_full";
            default -> null;
        };
        if (key != null) {
            Component hint = Component.translatable(key);
            graphics.text(this.font, hint, (this.imageWidth - this.font.width(hint)) / 2, 72, 0xFFFF8A8A, false);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int bx = this.leftPos + BAR_X;
        int by = this.topPos + BAR_Y;
        if (mouseX >= bx - 1 && mouseX <= bx + 4 && mouseY >= by && mouseY <= by + BAR_H) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.theabyss.station.uses", this.menu.usesLeft()), mouseX, mouseY);
        }
    }
}
