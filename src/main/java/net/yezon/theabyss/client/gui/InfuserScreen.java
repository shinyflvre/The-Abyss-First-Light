package net.yezon.theabyss.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.block.entity.SomniumInfuserBlockEntity;
import net.yezon.theabyss.world.inventory.InfuserMenu;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/infuser.png");
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/somnium_infuser_gui.png");
    private static final int[][] COIL = {
        {68, 13}, {69, 13}, {70, 14}, {62, 15}, {63, 15}, {64, 15}, {65, 15}, {66, 15}, {67, 15}, {70, 15}, {61, 16}, {68, 16}, {69, 16},
        {60, 17}, {67, 17}, {68, 17}, {70, 17}, {61, 18}, {62, 18}, {63, 18}, {64, 18}, {65, 18}, {66, 18}, {70, 18}, {61, 19}, {68, 19},
        {69, 19}, {60, 20}, {67, 20}, {68, 20}, {70, 20}, {61, 21}, {62, 21}, {63, 21}, {64, 21}, {65, 21}, {66, 21}, {70, 21}, {61, 22},
        {68, 22}, {69, 22}, {60, 23}, {67, 23}, {68, 23}, {70, 23}, {61, 24}, {62, 24}, {63, 24}, {64, 24}, {65, 24}, {66, 24}, {70, 24},
        {61, 25}, {68, 25}, {69, 25}, {60, 26}, {67, 26}, {68, 26}, {70, 26}, {61, 27}, {62, 27}, {63, 27}, {64, 27}, {65, 27}, {66, 27},
        {70, 27}, {61, 28}, {68, 28}, {69, 28}, {60, 29}, {67, 29}, {68, 29}, {70, 29}, {71, 29}, {72, 29}, {61, 30}, {62, 30}, {63, 30},
        {64, 30}, {65, 30}, {66, 30}
    };
    private static final int COIL_TOP = 13;
    private static final int COIL_BOTTOM = 30;
    private static final int[][] TUBES = {{85, 88}, {90, 93}};
    private static final int TUBE_TOP = 33;
    private static final int TUBE_BOTTOM = 53;
    private static final int FEED_Y = 54;
    private static final int[][] FEED_LEFT = {{44, 53}, {70, 84}};
    private static final int[][] FEED_RIGHT = {{131, 122}, {105, 92}};
    private static final int SOMNIUM = 0xFF6FF3FF;
    private static final int LIGHT = 0xFFE6FFFB;

    public InfuserScreen(InfuserMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth,
            this.imageHeight);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, 256, 256, 256, 256);
        int x0 = this.leftPos;
        int y0 = this.topPos;
        float time = (Util.getMillis() % 100000L) / 1000.0F;
        boolean brewing = this.menu.brewing();
        float progress = this.menu.progress();
        this.coil(graphics, x0, y0, this.menu.energy(), brewing, time);
        if (brewing) {
            this.feeds(graphics, x0, y0, Mth.clamp(progress * 2.0F, 0.0F, 1.0F));
            this.tubes(graphics, x0, y0, progress, time);
        }
    }

    private void coil(GuiGraphicsExtractor graphics, int x0, int y0, float energy, boolean brewing, float time) {
        float lit = energy <= 0.0F ? Float.MAX_VALUE : COIL_BOTTOM - energy * (COIL_BOTTOM - COIL_TOP + 1);
        float wave = brewing ? 0.5F + 0.5F * Mth.sin(time * 6.0F) : 0.0F;
        int core = mix(SOMNIUM, LIGHT, 0.4F + 0.6F * wave);
        int halo = Math.round(70 + 60 * wave) << 24 | 0x6FF3FF;
        for (int[] p : COIL) {
            if (p[1] >= lit) {
                graphics.fill(x0 + p[0] - 1, y0 + p[1] - 1, x0 + p[0] + 2, y0 + p[1] + 2, halo);
            }
        }
        for (int[] p : COIL) {
            int colour = p[1] >= lit ? 0xFF000000 | core : 0xFF1F5A60;
            graphics.fill(x0 + p[0], y0 + p[1], x0 + p[0] + 1, y0 + p[1] + 1, colour);
        }
    }

    private void feeds(GuiGraphicsExtractor graphics, int x0, int y0, float t) {
        for (int[][] side : new int[][][] {FEED_LEFT, FEED_RIGHT}) {
            int total = 0;
            for (int[] seg : side) {
                total += Math.abs(seg[1] - seg[0]) + 1;
            }
            int left = Math.round(t * total);
            for (int[] seg : side) {
                if (left <= 0) {
                    break;
                }
                int length = Math.abs(seg[1] - seg[0]) + 1;
                int step = Math.min(left, length);
                int dir = Integer.signum(seg[1] - seg[0]);
                int end = seg[0] + dir * (step - 1);
                int a = Math.min(seg[0], end);
                int b = Math.max(seg[0], end);
                graphics.fill(x0 + a, y0 + FEED_Y - 1, x0 + b + 1, y0 + FEED_Y + 2, SOMNIUM);
                graphics.fill(x0 + a, y0 + FEED_Y, x0 + b + 1, y0 + FEED_Y + 1, LIGHT);
                left -= step;
            }
        }
    }

    private void tubes(GuiGraphicsExtractor graphics, int x0, int y0, float progress, float time) {
        int height = TUBE_BOTTOM - TUBE_TOP + 1;
        int filled = Math.round(progress * height);
        if (filled <= 0) {
            return;
        }
        int bottom = TUBE_TOP + filled - 1;
        for (int t = 0; t < TUBES.length; t++) {
            int[] tube = TUBES[t];
            graphics.fill(x0 + tube[0], y0 + TUBE_TOP, x0 + tube[1] + 1, y0 + bottom + 1, 0xCC2FB8C8);
            graphics.fill(x0 + tube[0], y0 + bottom, x0 + tube[1] + 1, y0 + bottom + 1, LIGHT);
            for (int k = 0; k < 3; k++) {
                float phase = (time * 0.9F + k * 0.33F + t * 0.17F) % 1.0F;
                int by = bottom - Math.round(phase * (filled - 1));
                int bx = tube[0] + 1 + (k + t) % 2;
                if (by >= TUBE_TOP && by <= bottom) {
                    graphics.fill(x0 + bx, y0 + by, x0 + bx + 2, y0 + by + 1, LIGHT);
                }
            }
        }
    }

    private static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return r << 16 | g << 8 | bl;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("gui.theabyss.infuser.label_infuser"), 123, 7, -6684724, false);
        graphics.text(this.font, Component.translatable("gui.theabyss.infuser.label_inventory"), 8, 72, -6684724, false);
        String key = switch (this.menu.state()) {
            case SomniumInfuserBlockEntity.NO_ENERGY -> "gui.theabyss.station.no_energy";
            case SomniumInfuserBlockEntity.OUTPUT_FULL -> "gui.theabyss.station.output_full";
            default -> null;
        };
        if (key != null) {
            Component hint = Component.translatable(key);
            graphics.text(this.font, hint, 168 - this.font.width(hint), 72, 0xFFFF8A8A, false);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;
        if (x >= 58 && x <= 73 && y >= COIL_TOP && y <= COIL_BOTTOM) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.theabyss.station.uses", this.menu.usesLeft()), mouseX, mouseY);
        }
    }
}
