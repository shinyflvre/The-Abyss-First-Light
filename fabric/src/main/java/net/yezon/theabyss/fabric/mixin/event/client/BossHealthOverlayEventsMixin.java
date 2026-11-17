package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.platform.client.event.CustomizeGuiOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayEventsMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V"))
    private void theabyss$bossEventProgress(BossHealthOverlay overlay, GuiGraphics graphics, int x, int y, BossEvent event, Operation<Void> original,
        @Share("theabyss$bossCanceled") LocalBooleanRef canceled) {
        if (event instanceof LerpingBossEvent lerping) {
            CustomizeGuiOverlayEvent.BossEventProgress progress = ClientFabricEvents.bossEventProgress(graphics, lerping, x, y,
                10 + Minecraft.getInstance().font.lineHeight);
            if (progress != null && progress.isCanceled()) {
                canceled.set(true);
                return;
            }
        }
        canceled.set(false);
        original.call(overlay, graphics, x, y, event);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int theabyss$bossName(GuiGraphics graphics, Font font, Component text, int x, int y, int color, Operation<Integer> original,
        @Share("theabyss$bossCanceled") LocalBooleanRef canceled) {
        if (canceled.get()) {
            return 0;
        }
        return original.call(graphics, font, text, x, y, color);
    }
}
