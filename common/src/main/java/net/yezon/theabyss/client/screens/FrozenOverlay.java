package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class FrozenOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/frozen.png");

    private FrozenOverlay() {
    }

    public static void render(GuiGraphics graphics, LocalPlayer entity) {
        TheabyssModGuiLayers.blitFullScreenFaded(graphics, TEXTURE, TheabyssModVariables.getPlayerVariables(entity).Freeze);
    }
}
