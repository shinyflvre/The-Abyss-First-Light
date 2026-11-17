package net.yezon.theabyss.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.yezon.theabyss.client.GhostSpectatorMenuBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorGui.class)
public abstract class SpectatorGuiGhostMixin {
    @Inject(method = "onMouseMiddleClick", at = @At("HEAD"), cancellable = true)
    private void theabyss$ghostNoSpectatorMenu(CallbackInfo ci) {
        if (GhostSpectatorMenuBlock.blocks(Minecraft.getInstance().player)) {
            ci.cancel();
        }
    }
}
