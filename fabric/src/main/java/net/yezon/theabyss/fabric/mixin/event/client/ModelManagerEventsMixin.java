package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public abstract class ModelManagerEventsMixin {
    @Inject(method = "lambda$loadModels$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/sprite/MaterialBaker;logMissingTextures()V"))
    private static void theabyss$modifyBakingResult(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ModelBakery.BakingResult bakingResult) {
        ClientFabricEvents.modifyBakingResult(bakingResult);
    }
}
