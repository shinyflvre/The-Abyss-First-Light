package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityRideTickEventMixin {
    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void theabyss$rideTickEvents(Entity entity, Operation<Void> original) {
        if (ServerFabricEvents.entityTickPre(entity)) {
            return;
        }
        original.call(entity);
        ServerFabricEvents.entityTickPost(entity);
    }
}
