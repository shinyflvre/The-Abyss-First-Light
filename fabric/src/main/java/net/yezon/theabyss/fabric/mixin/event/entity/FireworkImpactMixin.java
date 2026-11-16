package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class FireworkImpactMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void theabyss$fireworkImpact(HitResult hit, CallbackInfo ci) {
        if ((Object)this instanceof FireworkRocketEntity firework && EntityFabricEvents.projectileImpact(firework, hit)) {
            ci.cancel();
        }
    }
}
