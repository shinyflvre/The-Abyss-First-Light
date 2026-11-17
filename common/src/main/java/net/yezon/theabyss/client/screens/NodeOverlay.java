package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.events.NodeOverlayEvents;

public final class NodeOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/node_gui_applied.png");

    private NodeOverlay() {
    }

    public static void render(GuiGraphics graphics, LocalPlayer entity) {
        if (NodeOverlayEvents.execute(entity)) {
            TheabyssModGuiLayers.blitFullScreen(graphics, TEXTURE, -1);
        }
    }
}
