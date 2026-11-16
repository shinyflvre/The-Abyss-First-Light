package net.yezon.theabyss.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.possession.client.PossessionClient;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TheabyssModGuiLayers {
    private TheabyssModGuiLayers() {
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, id("ghost_overlay"), whenHudShown(GhostOverlayOverlay::render));
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, id("intro_screen"), whenHudShown(IntroScreenOverlay::render));
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, id("node"), whenHudShown(NodeOverlay::render));
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, id("frozen"), whenHudShown(FrozenOverlay::render));
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, id("transformation"), whenHudShown(TransformationOverlay::render));

        event.registerBelow(VanillaGuiLayers.AIR_LEVEL, id("somnium_bar"), (graphics, deltaTracker) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !Minecraft.getInstance().gui.hud.isHidden() && !player.isSpectator()) {
                SomniumBarOverlay.render(graphics, player);
            }
        });
        event.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, id("possession_host_health"), PossessionClient::renderHostHealth);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("parry_cue"), net.yezon.theabyss.client.fx.ParryCue::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("night_sense"), net.yezon.theabyss.client.fx.NightGraceClient::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("eagle_boss_compass"),net.yezon.theabyss.client.fx.EagleBossCompass::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("spell_wheel"), net.yezon.theabyss.client.fx.SpellWheelClient::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("rewind_tunnel"), net.yezon.theabyss.client.fx.spell.RewindCamera::renderTunnel);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("chronicle_subtitles"), net.yezon.theabyss.client.lore.ChronicleSubtitles::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, id("dialogue"), net.yezon.theabyss.client.dialogue.DialogueClient::render);
    }

    @FunctionalInterface
    interface PlayerOverlay {
        void render(GuiGraphicsExtractor graphics, LocalPlayer player);
    }

    private static GuiLayer whenHudShown(PlayerOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && !minecraft.gui.hud.isHidden()) {
                overlay.render(graphics, minecraft.player);
            }
        };
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, path);
    }

    static void blitFullScreen(GuiGraphicsExtractor graphics, Identifier texture, int color) {
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F, w, h, w, h, color);
    }

    static void blitFullScreenFaded(GuiGraphicsExtractor graphics, Identifier texture, double strength) {
        float s = Mth.clamp((float)strength, 0.0F, 1.0F);
        if (s > 0.0F) {
            blitFullScreen(graphics, texture, ARGB.colorFromFloat(s, s, s, s));
        }
    }
}
