package net.yezon.theabyss.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.fabric.client.FabricHudHeights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HudHeightsMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void theabyss$beginFrame(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        FabricHudHeights.beginFrame();
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void theabyss$airY(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        original.call(graphics, sprite, x, FabricHudHeights.airY(graphics.guiHeight(), y), width, height);
    }
}
