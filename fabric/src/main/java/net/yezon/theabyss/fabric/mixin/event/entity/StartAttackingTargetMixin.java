package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAttacking.class)
public abstract class StartAttackingTargetMixin {
    @Inject(method = "method_47123", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"), cancellable = true)
    private static void theabyss$changeTarget(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) Mob body, @Local LivingEntity target) {
        if (LivingFabricEvents.changeTarget(body, target)) {
            cir.setReturnValue(false);
        }
    }
}
