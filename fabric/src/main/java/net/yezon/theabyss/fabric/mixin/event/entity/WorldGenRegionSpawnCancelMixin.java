package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionSpawnCancelMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void theabyss$spawnCancelled(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Mob mob && EntityFabricEvents.isSpawnCancelled(mob)) {
            cir.setReturnValue(false);
        }
    }
}
