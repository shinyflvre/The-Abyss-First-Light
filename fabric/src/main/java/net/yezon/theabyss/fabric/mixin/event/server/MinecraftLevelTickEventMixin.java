package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftLevelTickEventMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void theabyss$levelTickPost(ClientLevel level, BooleanSupplier haveTime, Operation<Void> original) {
        original.call(level, haveTime);
        ServerFabricEvents.levelTickPost(level);
    }
}
