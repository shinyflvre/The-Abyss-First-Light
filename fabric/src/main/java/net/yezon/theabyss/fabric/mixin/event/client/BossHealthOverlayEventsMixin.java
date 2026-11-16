package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayEventsMixin {
    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V"))
    private void theabyss$bossEventProgress(BossHealthOverlay overlay, GuiGraphicsExtractor graphics, int x, int y, BossEvent event, Operation<Void> original,
        @Share("theabyss$bossCanceled") LocalBooleanRef canceled) {
        if (event instanceof LerpingBossEvent lerping && ClientFabricEvents.bossEventProgress(lerping, graphics, x, y)) {
            canceled.set(true);
            return;
        }
        canceled.set(false);
        original.call(overlay, graphics, x, y, event);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void theabyss$bossName(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, Operation<Void> original,
        @Share("theabyss$bossCanceled") LocalBooleanRef canceled) {
        if (canceled.get()) {
            return;
        }
        original.call(graphics, font, text, x, y, color);
    }
}
