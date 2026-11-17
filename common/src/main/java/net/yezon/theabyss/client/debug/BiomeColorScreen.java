package net.yezon.theabyss.client.debug;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraft.world.level.biome.Biome;
import net.yezon.theabyss.compat.GuiGraphicsCompat;

public class BiomeColorScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int ROW_HEIGHT = 24;
    private static final long REBUILD_DELAY_MS = 300L;

    private final Holder<Biome> biome;
    private final List<Row> rows = new ArrayList<>();
    private long rebuildAt = -1L;
    private Component status = Component.empty();

    public BiomeColorScreen(Holder<Biome> biome) {
        super(Component.literal("Biome Colors"));
        this.biome = biome;
    }

    private int panelLeft() {
        return this.width - PANEL_WIDTH - 6;
    }

    @Override
    protected void init() {
        this.rows.clear();
        int x = this.panelLeft() + 6;
        int y = 34;
        for (BiomeColors.Channel channel : BiomeColors.Channel.values()) {
            this.rows.add(new Row(channel, x, y));
            y += ROW_HEIGHT;
        }
        int buttonY = y + 8;
        int third = (PANEL_WIDTH - 12 - 8) / 3;
        this.addRenderableWidget(Button.builder(Component.literal("Copy all"), b -> {
            this.minecraft.keyboardHandler.setClipboard(BiomeColors.export());
            this.status = Component.literal("Copied " + BiomeColors.editedBiomes() + " edited biome(s) to the clipboard");
        }).bounds(x, buttonY, third, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset biome"), b -> {
            BiomeColors.resetBiome(this.biome);
            this.rows.forEach(Row::refresh);
            this.rebuildAt = Util.getMillis();
            this.status = Component.literal("Biome reset to its original colours");
        }).bounds(x + third + 4, buttonY, third, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(x + 2 * (third + 4), buttonY, third, 20).build());
    }

    @Override
    public void tick() {
        if (this.rebuildAt >= 0L && Util.getMillis() >= this.rebuildAt) {
            this.rebuildAt = -1L;
            if (this.minecraft.level != null) {
                this.minecraft.level.clearTintCaches();
                this.minecraft.levelRenderer.allChanged();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        int left = this.panelLeft();
        int bottom = 34 + BiomeColors.Channel.values().length * ROW_HEIGHT + 48;
        graphics.fill(left, 4, left + PANEL_WIDTH, bottom, 0xC0101418);
        graphics.renderOutline(left, 4, PANEL_WIDTH, bottom - 4, 0xFF3A4A55);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        int left = this.panelLeft() + 6;
        GuiGraphicsCompat.text(graphics, this.font, "Biome Colors", left, 10, 0xFFFFFFFF, true);
        GuiGraphicsCompat.text(graphics, this.font, BiomeColors.id(this.biome).toString(), left, 21, 0xFF7FD4E6, true);
        for (Row row : this.rows) {
            row.draw(graphics);
        }
        int statusY = 34 + this.rows.size() * ROW_HEIGHT + 34;
        GuiGraphicsCompat.text(graphics, this.font, this.status, left, statusY, 0xFFB0B0B0, true);
    }

    private final class Row {
        private final BiomeColors.Channel channel;
        private final int x;
        private final int y;
        private final EditBox hex;
        private final ChannelSlider[] sliders = new ChannelSlider[3];
        private boolean updating;
        private int rgb;

        Row(BiomeColors.Channel channel, int x, int y) {
            this.channel = channel;
            this.x = x;
            this.y = y;
            this.rgb = BiomeColors.current(minecraft, biome, channel);
            this.hex = new EditBox(font, x + 76, y, 58, 18, Component.literal(channel.label));
            this.hex.setMaxLength(7);
            this.hex.setValue(BiomeColors.hex(this.rgb));
            this.hex.setResponder(text -> {
                if (this.updating) {
                    return;
                }
                try {
                    this.change(BiomeColors.parseHex(text), false);
                    this.hex.setTextColor(0xFFE0E0E0);
                } catch (NumberFormatException invalid) {
                    this.hex.setTextColor(0xFFFF6060);
                }
            });
            addRenderableWidget(this.hex);
            for (int i = 0; i < 3; i++) {
                this.sliders[i] = new ChannelSlider(this, i, x + 138 + i * 46, y, 44);
                addRenderableWidget(this.sliders[i]);
            }
            addRenderableWidget(Button.builder(Component.literal("↺"), b -> {
                BiomeColors.reset(biome, this.channel);
                this.refresh();
                if (this.channel.baked) {
                    rebuildAt = Util.getMillis();
                }
            }).bounds(x + 138 + 3 * 46, y, 16, 18).build());
        }

        void change(int newRgb, boolean fromSlider) {
            this.rgb = newRgb;
            if (BiomeColors.set(biome, this.channel, newRgb)) {
                rebuildAt = Util.getMillis() + REBUILD_DELAY_MS;
            }
            this.updating = true;
            if (fromSlider) {
                this.hex.setValue(BiomeColors.hex(newRgb));
            } else {
                for (ChannelSlider slider : this.sliders) {
                    slider.sync();
                }
            }
            this.updating = false;
        }

        void refresh() {
            this.rgb = BiomeColors.current(minecraft, biome, this.channel);
            this.updating = true;
            this.hex.setValue(BiomeColors.hex(this.rgb));
            for (ChannelSlider slider : this.sliders) {
                slider.sync();
            }
            this.updating = false;
        }

        void draw(GuiGraphics graphics) {
            boolean edited = BiomeColors.isEdited(biome, this.channel);
            GuiGraphicsCompat.text(graphics, font, this.channel.label, this.x, this.y + 5, edited ? 0xFFFFD27F : 0xFFE0E0E0, true);
            graphics.fill(this.x + 56, this.y + 1, this.x + 72, this.y + 17, 0xFF000000 | this.rgb);
            graphics.renderOutline(this.x + 56, this.y + 1, 16, 16, 0xFF808080);
        }
    }

    private static final class ChannelSlider extends AbstractSliderButton {
        private static final String[] NAMES = {"R", "G", "B"};
        private final Row row;
        private final int index;

        ChannelSlider(Row row, int index, int x, int y, int width) {
            super(x, y, width, 18, Component.empty(), (row.rgb >> (16 - index * 8) & 255) / 255.0);
            this.row = row;
            this.index = index;
            this.updateMessage();
        }

        private int channelValue() {
            return (int)Math.round(this.value * 255.0);
        }

        void sync() {
            this.value = (this.row.rgb >> (16 - this.index * 8) & 255) / 255.0;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(NAMES[this.index] + " " + this.channelValue()));
        }

        @Override
        protected void applyValue() {
            if (this.row.updating) {
                return;
            }
            int shift = 16 - this.index * 8;
            int rgb = this.row.rgb & ~(255 << shift) | this.channelValue() << shift;
            this.row.change(rgb, true);
        }
    }
}
