package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class ArrowImpactMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    private ProjectileDeflection theabyss$impact(AbstractArrow arrow, HitResult hit, Operation<ProjectileDeflection> original,
        @Share("theabyssImpactCancelled") LocalBooleanRef cancelled) {
        if (EntityFabricEvents.projectileImpact(arrow, hit)) {
            cancelled.set(true);
            return ProjectileDeflection.REVERSE;
        }
        cancelled.set(false);
        return original.call(arrow, hit);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hasImpulse:Z", opcode = Opcodes.PUTFIELD))
    private boolean theabyss$impulseAfterImpact(AbstractArrow arrow, boolean value, @Share("theabyssImpactCancelled") LocalBooleanRef cancelled) {
        return !cancelled.get();
    }
}
