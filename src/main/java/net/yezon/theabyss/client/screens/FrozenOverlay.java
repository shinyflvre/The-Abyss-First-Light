package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class FrozenOverlay {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/frozen.png");

    private FrozenOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, LocalPlayer entity) {
        TheabyssModGuiLayers.blitFullScreenFaded(graphics, TEXTURE, TheabyssModVariables.getPlayerVariables(entity).Freeze);
    }
}
