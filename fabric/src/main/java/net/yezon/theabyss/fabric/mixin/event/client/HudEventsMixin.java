package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.client.FabricHudHeights;
import net.yezon.theabyss.fabric.event.FabricHudLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class HudEventsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean theabyss$airShown;
    @Unique
    private boolean theabyss$airCanceled;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDraw;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$renderGui(LayeredDraw layers, GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        if (FabricHudLayers.guiPre(graphics, delta)) {
            return;
        }
        original.call(layers, graphics, delta);
        FabricHudLayers.guiPost(graphics, delta);
    }

    @WrapMethod(method = "renderCameraOverlays")
    private void theabyss$cameraOverlays(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.CAMERA_OVERLAYS, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderCrosshair")
    private void theabyss$crosshair(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.CROSSHAIR, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderExperienceLevel")
    private void theabyss$experienceLevel(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.EXPERIENCE_LEVEL, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderEffects")
    private void theabyss$effects(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.EFFECTS, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderSleepOverlay")
    private void theabyss$sleepOverlay(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        boolean hidden = this.minecraft.options.hideGui;
        if (hidden) {
            FabricHudLayers.hiddenFirstBlock(graphics, delta);
        }
        FabricHudLayers.vanilla(FabricHudLayers.SLEEP_OVERLAY, graphics, delta, () -> original.call(graphics, delta));
        if (hidden) {
            FabricHudLayers.hiddenSecondBlock(graphics, delta);
        }
    }

    @WrapMethod(method = "renderDemoOverlay")
    private void theabyss$demoOverlay(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.DEMO_OVERLAY, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderScoreboardSidebar")
    private void theabyss$scoreboard(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.SCOREBOARD_SIDEBAR, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderOverlayMessage")
    private void theabyss$overlayMessage(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.OVERLAY_MESSAGE, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderTitle")
    private void theabyss$title(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.TITLE, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderChat")
    private void theabyss$chat(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.CHAT, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapMethod(method = "renderTabList")
    private void theabyss$tabList(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.TAB_LIST, graphics, delta, () -> original.call(graphics, delta));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void theabyss$spectatorHotbar(SpectatorGui gui, GuiGraphics graphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.HOTBAR, graphics, delta, () -> original.call(gui, graphics));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void theabyss$itemHotbar(Gui gui, GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.HOTBAR, graphics, delta, () -> original.call(gui, graphics, delta));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    private void theabyss$jumpMeter(Gui gui, PlayerRideableJumping jumping, GuiGraphics graphics, int x, Operation<Void> original,
        @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.JUMP_METER, graphics, delta, () -> original.call(gui, jumping, graphics, x));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    private void theabyss$experienceBar(Gui gui, GuiGraphics graphics, int x, Operation<Void> original, @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.EXPERIENCE_BAR, graphics, delta, () -> original.call(gui, graphics, x));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean theabyss$survivalLayers(MultiPlayerGameMode gameMode, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphics graphics,
        @Local(argsOnly = true) DeltaTracker delta) {
        boolean survival = original.call(gameMode);
        if (!survival) {
            FabricHudLayers.skipped(FabricHudLayers.PLAYER_HEALTH, graphics, delta);
            FabricHudLayers.skipped(FabricHudLayers.ARMOR_LEVEL, graphics, delta);
            FabricHudLayers.skipped(FabricHudLayers.FOOD_LEVEL, graphics, delta);
            FabricHudLayers.events(FabricHudLayers.AIR_LEVEL, graphics, delta);
        }
        return survival;
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void theabyss$vehicleHealth(Gui gui, GuiGraphics graphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.VEHICLE_HEALTH, graphics, delta, () -> original.call(gui, graphics));
        FabricHudLayers.before(FabricHudLayers.AIR_LEVEL, graphics, delta);
        FabricHudLayers.after(FabricHudLayers.AIR_LEVEL, graphics, delta);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void theabyss$selectedItemName(Gui gui, GuiGraphics graphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.SELECTED_ITEM_NAME, graphics, delta, () -> original.call(gui, graphics));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void theabyss$spectatorTooltip(SpectatorGui gui, GuiGraphics graphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker delta) {
        FabricHudLayers.vanilla(FabricHudLayers.SPECTATOR_TOOLTIP, graphics, delta, () -> original.call(gui, graphics));
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void theabyss$armorLevel(GuiGraphics graphics, Player player, int yLineBase, int rows, int rowHeight, int xLeft, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.ARMOR_LEVEL, graphics, FabricHudLayers.delta(), () -> original.call(graphics, player, yLineBase, rows, rowHeight, xLeft));
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    private void theabyss$playerHealth(Gui gui, GuiGraphics graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex,
        float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, Operation<Void> original) {
        FabricHudHeights.hearts(graphics.guiHeight(), yLineBase);
        FabricHudLayers.vanilla(FabricHudLayers.PLAYER_HEALTH, graphics, FabricHudLayers.delta(),
            () -> original.call(gui, graphics, player, xLeft, yLineBase, healthRowHeight, heartOffsetIndex, maxHealth, currentHealth, oldHealth, absorption, blink));
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int theabyss$foodSkipped(Gui gui, LivingEntity vehicle, Operation<Integer> original, @Local(argsOnly = true) GuiGraphics graphics) {
        int hearts = original.call(gui, vehicle);
        if (hearts != 0) {
            FabricHudLayers.skipped(FabricHudLayers.FOOD_LEVEL, graphics, FabricHudLayers.delta());
        }
        return hearts;
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void theabyss$foodLevel(Gui gui, GuiGraphics graphics, Player player, int yLineBase, int xRight, Operation<Void> original) {
        FabricHudLayers.vanilla(FabricHudLayers.FOOD_LEVEL, graphics, FabricHudLayers.delta(), () -> original.call(gui, graphics, player, yLineBase, xRight));
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"))
    private void theabyss$airPre(ProfilerFiller profiler, String section, Operation<Void> original, @Local(argsOnly = true) GuiGraphics graphics) {
        original.call(profiler, section);
        if ("air".equals(section)) {
            this.theabyss$airCanceled = FabricHudLayers.layerPre(graphics, FabricHudLayers.delta(), FabricHudLayers.AIR_LEVEL);
            this.theabyss$airShown = !this.theabyss$airCanceled;
        }
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void theabyss$airBubble(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (!this.theabyss$airCanceled) {
            original.call(graphics, sprite, x, y, width, height);
        }
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private void theabyss$airPost(ProfilerFiller profiler, Operation<Void> original, @Local(argsOnly = true) GuiGraphics graphics) {
        if (this.theabyss$airShown) {
            FabricHudLayers.layerPost(graphics, FabricHudLayers.delta(), FabricHudLayers.AIR_LEVEL);
        }
        this.theabyss$airShown = false;
        this.theabyss$airCanceled = false;
        original.call(profiler);
    }
}
