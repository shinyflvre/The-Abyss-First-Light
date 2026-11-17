package net.yezon.theabyss.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.minecraft.util.Mth;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.client.gui.VanillaGuiLayers;
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
            if (player != null && !hudHidden() && !player.isSpectator()) {
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
        void render(GuiGraphics graphics, LocalPlayer player);
    }

    static boolean hudHidden() {
        return Minecraft.getInstance().options.hideGui;
    }

    private static GuiLayer whenHudShown(PlayerOverlay overlay) {
        return (graphics, deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && !hudHidden()) {
                overlay.render(graphics, minecraft.player);
            }
        };
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, path);
    }

    static void blitFullScreen(GuiGraphics graphics, ResourceLocation texture, int color) {
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        GuiGraphicsCompat.blit(graphics, texture, 0, 0, 0.0F, 0.0F, w, h, w, h, color);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    static void blitFullScreenFaded(GuiGraphics graphics, ResourceLocation texture, double strength) {
        float s = Mth.clamp((float)strength, 0.0F, 1.0F);
        if (s > 0.0F) {
            blitFullScreen(graphics, texture, ARGB.colorFromFloat(s, s, s, s));
        }
    }
}
