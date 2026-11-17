package net.yezon.theabyss.fabric.mixin.event.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerEventsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void theabyss$mouseScrolling(long window, double xOffset, double yOffset, CallbackInfo ci) {
        boolean discrete = this.minecraft.options.discreteMouseScroll().get();
        double sensitivity = this.minecraft.options.mouseWheelSensitivity().get();
        double scrollY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
        if (ClientFabricEvents.mouseScrolling(scrollY)) {
            ci.cancel();
        }
    }
}
