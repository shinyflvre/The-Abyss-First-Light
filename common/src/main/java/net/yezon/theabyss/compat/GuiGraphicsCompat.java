package net.yezon.theabyss.compat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public final class GuiGraphicsCompat {
    private GuiGraphicsCompat() {
    }

    public static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(g, texture, x, y, u, v, width, height, width, height, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight,
        int color) {
        blit(g, texture, x, y, u, v, width, height, width, height, textureWidth, textureHeight, color);
    }

    public static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight,
        int textureWidth, int textureHeight) {
        blit(g, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth, textureHeight, -1);
    }

    public static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int srcWidth, int srcHeight,
        int textureWidth, int textureHeight, int color) {
        if (width == 0 || height == 0 || color >>> 24 == 0) {
            return;
        }
        boolean tinted = color != -1;
        if (tinted) {
            setColor(g, color);
        }
        beginTranslucent();
        g.blit(texture, x, y, width, height, u, v, srcWidth, srcHeight, textureWidth, textureHeight);
        endTranslucent();
        if (tinted) {
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static void blitSprite(GuiGraphics g, ResourceLocation sprite, int x, int y, int width, int height) {
        blitSprite(g, sprite, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics g, ResourceLocation sprite, int x, int y, int width, int height, int color) {
        if (width == 0 || height == 0 || color >>> 24 == 0) {
            return;
        }
        boolean tinted = color != -1;
        if (tinted) {
            setColor(g, color);
        }
        beginTranslucent();
        g.blitSprite(sprite, x, y, width, height);
        endTranslucent();
        if (tinted) {
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static void text(GuiGraphics g, Font font, String text, int x, int y, int color, boolean shadow) {
        if (text != null && visible(color)) {
            g.drawString(font, text, x, y, color, shadow);
        }
    }

    public static void text(GuiGraphics g, Font font, Component text, int x, int y, int color, boolean shadow) {
        text(g, font, text.getVisualOrderText(), x, y, color, shadow);
    }

    public static void text(GuiGraphics g, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        if (visible(color)) {
            g.drawString(font, text, x, y, color, shadow);
        }
    }

    public static void centeredText(GuiGraphics g, Font font, String text, int x, int y, int color) {
        text(g, font, text, x - font.width(text) / 2, y, color, true);
    }

    public static void centeredText(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        FormattedCharSequence sequence = text.getVisualOrderText();
        text(g, font, sequence, x - font.width(sequence) / 2, y, color, true);
    }

    public static void beginTranslucent() {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void endTranslucent() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static boolean visible(int color) {
        return color >>> 24 >= 4;
    }

    private static void setColor(GuiGraphics g, int color) {
        g.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >>> 24) / 255.0F);
    }
}
