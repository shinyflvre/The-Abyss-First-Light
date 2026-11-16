package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.yezon.theabyss.fabric.event.EntityFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public abstract class EntityJoinLevelMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void theabyss$joinLevel(EntityAccess access, boolean loaded, CallbackInfoReturnable<Boolean> cir) {
        if (access instanceof Entity entity && EntityFabricEvents.joinLevel(entity, entity.level(), loaded)) {
            cir.setReturnValue(false);
        }
    }
}
