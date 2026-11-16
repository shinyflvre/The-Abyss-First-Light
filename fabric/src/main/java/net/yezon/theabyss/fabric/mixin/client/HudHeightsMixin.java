package net.yezon.theabyss.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.client.FabricHudHeights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudHeightsMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void theabyss$beginFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        FabricHudHeights.beginFrame();
    }

    @Inject(method = "extractHearts", at = @At("HEAD"))
    private void theabyss$heartsY(GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex,
        float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci) {
        FabricHudHeights.hearts(graphics.guiHeight(), yLineBase);
    }

    @WrapMethod(method = "extractAirBubbles")
    private void theabyss$airY(GuiGraphicsExtractor graphics, Player player, int vehicleHearts, int yLineAir, int xRight, Operation<Void> original) {
        original.call(graphics, player, vehicleHearts, FabricHudHeights.airY(graphics.guiHeight(), yLineAir), xRight);
    }
}
