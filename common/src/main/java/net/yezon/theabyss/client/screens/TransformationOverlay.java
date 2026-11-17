package net.yezon.theabyss.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class TransformationOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/screens/transformation.png");

    private TransformationOverlay() {
    }

    public static void render(GuiGraphics graphics, LocalPlayer entity) {
        double strength = TheabyssModVariables.getPlayerVariables(entity).Infected;
        if (net.yezon.theabyss.client.fx.OvergrowthClient.active()) {
            strength = 0.78 + 0.22 * net.yezon.theabyss.client.fx.OvergrowthClient.pulse();
        }
        TheabyssModGuiLayers.blitFullScreenFaded(graphics, TEXTURE, strength);
    }
}
