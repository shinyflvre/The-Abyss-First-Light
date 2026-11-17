package net.yezon.theabyss.mixin;

import net.minecraft.world.entity.monster.EnderMan;
import net.yezon.theabyss.entity.EndSpiderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EndSpiderTeleportMixin {
    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void theabyss$endSpiderTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof EndSpiderEntity spider) {
            cir.setReturnValue(spider.teleportSpider(x, y, z));
        }
    }
}
