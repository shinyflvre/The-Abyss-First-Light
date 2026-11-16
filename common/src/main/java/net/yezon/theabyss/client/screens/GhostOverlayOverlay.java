package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.events.ApplyGhostOverlayEvents;

public final class GhostOverlayOverlay {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/ghost_vision.png");

    private GhostOverlayOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, LocalPlayer entity) {
        if (ApplyGhostOverlayEvents.execute(entity)) {
            TheabyssModGuiLayers.blitFullScreen(graphics, TEXTURE, -1);
        }
    }
}
