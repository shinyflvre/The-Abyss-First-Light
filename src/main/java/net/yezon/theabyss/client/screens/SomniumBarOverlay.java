package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class SomniumBarOverlay {
    private static final Identifier MANA_NONE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/mana_none_icon.png");
    private static final Identifier MANA_HALF = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/mana_half_icon.png");
    private static final Identifier MANA_FULL = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/mana_icon.png");
    private static final int ICONS = 10;

    private SomniumBarOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, LocalPlayer entity) {
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
        int hudX = (int)vars.HudX;
        int hudY = (int)vars.HudY;
        int left = graphics.guiWidth() / 2 + 10 + hudX;
        int top = graphics.guiHeight() - 49 + hudY;
        double mana = vars.Mana;
        for (int i = 0; i < ICONS; i++) {
            icon(graphics, MANA_NONE, left + i * 8, top);
        }

        for (int i = 0; i < ICONS; i++) {
            if (mana > i * 10 + 4) {
                icon(graphics, MANA_HALF, left + i * 8, top);
            }
        }

        for (int i = 0; i < ICONS; i++) {
            if (mana > i * 10 + 9) {
                icon(graphics, MANA_FULL, left + i * 8, top);
            }
        }

        if (hudX == 0 && hudY == 0) {
            Hud hud = Minecraft.getInstance().gui.hud;
            hud.rightHeight = Math.max(hud.rightHeight, 59);
        }
    }

    private static void icon(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, 9, 9, 9, 9);
    }
}
