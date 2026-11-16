package net.yezon.theabyss.fabric.mixin.fluids.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererFluidMixin {
    @ModifyExpressionValue(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z")
    )
    private boolean theabyss$swimPoseInOwnFluids(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        return original || FluidContact.height(entity, FluidType::canSwim) > 0.0;
    }
}
