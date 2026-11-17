package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractHurtingProjectile.class)
public abstract class HurtingProjectileImpactMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;"))
    private HitResult theabyss$impact(HitResult hit) {
        Projectile self = (Projectile)(Object)this;
        if (EntityFabricEvents.projectileImpact(self, hit)) {
            return BlockHitResult.miss(hit.getLocation(), self.getDirection(), self.blockPosition());
        }
        return hit;
    }
}
