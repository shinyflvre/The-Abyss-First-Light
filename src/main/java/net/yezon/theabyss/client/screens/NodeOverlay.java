package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.events.NodeOverlayEvents;

public final class NodeOverlay {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/screens/node_gui_applied.png");

    private NodeOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, LocalPlayer entity) {
        if (NodeOverlayEvents.execute(entity)) {
            TheabyssModGuiLayers.blitFullScreen(graphics, TEXTURE, -1);
        }
    }
}
