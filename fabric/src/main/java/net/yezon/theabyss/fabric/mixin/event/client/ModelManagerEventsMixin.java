package net.yezon.theabyss.fabric.mixin.event.client;

import java.util.Map;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public abstract class ModelManagerEventsMixin {
    @Inject(method = "loadModels", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
        args = "ldc=dispatch"))
    private void theabyss$modifyBakingResult(ProfilerFiller profiler, Map<ResourceLocation, AtlasSet.StitchResult> atlases, ModelBakery bakery,
        CallbackInfoReturnable<?> cir) {
        ClientFabricEvents.modifyBakingResult(bakery.getBakedTopLevelModels(), bakery);
    }
}
