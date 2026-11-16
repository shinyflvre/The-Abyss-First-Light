package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobTargetMixin {
    @WrapOperation(method = "setTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;target:Lnet/minecraft/world/entity/LivingEntity;",
        opcode = Opcodes.PUTFIELD))
    private void theabyss$changeTarget(Mob mob, @Nullable LivingEntity target, Operation<Void> original) {
        if (!LivingFabricEvents.changeTarget(mob, target)) {
            original.call(mob, target);
        }
    }
}
