package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerEventsMixin {
    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;onMouseScroll(DD)Lorg/joml/Vector2i;"))
    private Vector2i theabyss$captureScroll(ScrollWheelHandler handler, double scaledX, double scaledY, Operation<Vector2i> original,
        @Share("theabyss$scrollY") LocalDoubleRef scrollY) {
        scrollY.set(scaledY);
        return original.call(handler, scaledX, scaledY);
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void theabyss$mouseScrolling(long handle, double xoffset, double yoffset, CallbackInfo ci, @Share("theabyss$scrollY") LocalDoubleRef scrollY) {
        if (ClientFabricEvents.mouseScrolling(scrollY.get())) {
            ci.cancel();
        }
    }
}
