package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.fabric.event.FabricHudLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudEventsMixin {
    @Shadow
    private boolean isHidden;

    @Inject(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;isHudHidden:Z", opcode = 181,
        shift = At.Shift.AFTER), cancellable = true)
    private void theabyss$renderGuiPre(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClientFabricEvents.guiActive() && ClientFabricEvents.renderGuiPre()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void theabyss$renderGuiPost(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClientFabricEvents.guiActive()) {
            ClientFabricEvents.renderGuiPost();
        }
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$hiddenFirstBlock(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.isHidden) {
            FabricHudLayers.hiddenFirstBlock(graphics, deltaTracker);
        }
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
        shift = At.Shift.AFTER))
    private void theabyss$hiddenSecondBlock(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.isHidden) {
            FabricHudLayers.hiddenSecondBlock(graphics);
        }
    }

    private static boolean theabyss$pre(GuiGraphicsExtractor graphics, Identifier layer) {
        return ClientFabricEvents.guiLayerPre(graphics, layer);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$cameraOverlays(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.CAMERA_OVERLAYS)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.CAMERA_OVERLAYS);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$crosshair(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.CROSSHAIR)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.CROSSHAIR);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;nextStratum()V", ordinal = 0))
    private void theabyss$afterCameraDecorations(GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.AFTER_CAMERA_DECORATIONS)) {
            original.call(graphics);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.AFTER_CAMERA_DECORATIONS);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$effects(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.EFFECTS)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.EFFECTS);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractBossOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$bossOverlay(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.BOSS_OVERLAY)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.BOSS_OVERLAY);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$sleepOverlay(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.SLEEP_OVERLAY)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.SLEEP_OVERLAY);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractDemoOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$demoOverlay(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.DEMO_OVERLAY)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.DEMO_OVERLAY);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$scoreboard(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.SCOREBOARD_SIDEBAR)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.SCOREBOARD_SIDEBAR);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractOverlayMessage(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$overlayMessage(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.OVERLAY_MESSAGE)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.OVERLAY_MESSAGE);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$title(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.TITLE)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.TITLE);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$chat(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.CHAT)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.CHAT);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$tabList(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.TAB_LIST)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.TAB_LIST);
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSubtitleOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Z)V"))
    private void theabyss$subtitles(Hud hud, GuiGraphicsExtractor graphics, boolean deferRendering, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.SUBTITLE_OVERLAY)) {
            original.call(hud, graphics, deferRendering);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.SUBTITLE_OVERLAY);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void theabyss$spectatorHotbar(SpectatorGui gui, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.HOTBAR)) {
            original.call(gui, graphics);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.HOTBAR);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$itemHotbar(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.HOTBAR)) {
            original.call(hud, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.HOTBAR);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean theabyss$survivalLayers(MultiPlayerGameMode gameMode, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
        boolean survival = original.call(gameMode);
        if (!survival) {
            ClientFabricEvents.guiLayerSkipped(graphics, FabricHudLayers.PLAYER_HEALTH, FabricHudLayers.ARMOR_LEVEL, FabricHudLayers.FOOD_LEVEL, FabricHudLayers.AIR_LEVEL);
        }
        return survival;
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractVehicleHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void theabyss$vehicleHealth(Hud hud, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.VEHICLE_HEALTH)) {
            original.call(hud, graphics);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.VEHICLE_HEALTH);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$contextualBackground(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.CONTEXTUAL_INFO_BAR_BACKGROUND)) {
            original.call(bar, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.CONTEXTUAL_INFO_BAR_BACKGROUND);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void theabyss$experienceLevel(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.EXPERIENCE_LEVEL)) {
            original.call(graphics, font, level);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.EXPERIENCE_LEVEL);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$contextualBar(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.CONTEXTUAL_INFO_BAR)) {
            original.call(bar, graphics, delta);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.CONTEXTUAL_INFO_BAR);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void theabyss$selectedItemName(Hud hud, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.SELECTED_ITEM_NAME)) {
            original.call(hud, graphics);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.SELECTED_ITEM_NAME);
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractAction(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void theabyss$spectatorTooltip(SpectatorGui gui, GuiGraphicsExtractor graphics, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.SPECTATOR_TOOLTIP)) {
            original.call(gui, graphics);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.SPECTATOR_TOOLTIP);
        }
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void theabyss$armorLevel(GuiGraphicsExtractor graphics, Player player, int yLineBase, int rows, int rowHeight, int xLeft, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.ARMOR_LEVEL)) {
            original.call(graphics, player, yLineBase, rows, rowHeight, xLeft);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.ARMOR_LEVEL);
        }
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void theabyss$playerHealth(Hud hud, GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex,
        float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.PLAYER_HEALTH)) {
            original.call(hud, graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth, currentHealth, oldHealth, absorption, blink);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.PLAYER_HEALTH);
        }
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void theabyss$foodLevel(Hud hud, GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.FOOD_LEVEL)) {
            original.call(hud, graphics, player, yLineBase, xRight);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.FOOD_LEVEL);
        }
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
    private void theabyss$airLevel(Hud hud, GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, Operation<Void> original) {
        if (!theabyss$pre(graphics, FabricHudLayers.AIR_LEVEL)) {
            original.call(hud, graphics, player, vehicleHearts, yLineAir, xRight);
            ClientFabricEvents.guiLayerPost(graphics, FabricHudLayers.AIR_LEVEL);
        }
    }
}
