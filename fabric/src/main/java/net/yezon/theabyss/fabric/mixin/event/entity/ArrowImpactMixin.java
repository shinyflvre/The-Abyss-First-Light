package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class ArrowImpactMixin {
    @Inject(method = "stepMoveAndHit", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"),
        cancellable = true)
    private void theabyss$blockImpact(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (EntityFabricEvents.projectileImpact((AbstractArrow)(Object)this, blockHitResult)) {
            ci.cancel();
        }
    }

    @Inject(method = "stepMoveAndHit", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;hitTargetsOrDeflectSelf(Ljava/util/Collection;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"),
        cancellable = true)
    private void theabyss$entityImpact(BlockHitResult blockHitResult, CallbackInfo ci, @Local(name = "firstEntityHit") EntityHitResult firstEntityHit) {
        if (EntityFabricEvents.projectileImpact((AbstractArrow)(Object)this, firstEntityHit)) {
            ci.cancel();
        }
    }
}
