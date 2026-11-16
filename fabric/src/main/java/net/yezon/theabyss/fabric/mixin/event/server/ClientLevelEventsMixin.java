package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelEventsMixin {
    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;commonTick()V"))
    private void theabyss$entityTickPre(Entity entity, Operation<Void> original, @Share("theabyss$tickCanceled") LocalBooleanRef canceled) {
        if (ServerFabricEvents.entityTickPre(entity)) {
            canceled.set(true);
            return;
        }
        original.call(entity);
    }

    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void theabyss$entityTickPost(Entity entity, Operation<Void> original, @Share("theabyss$tickCanceled") LocalBooleanRef canceled) {
        if (canceled.get()) {
            return;
        }
        original.call(entity);
        ServerFabricEvents.entityTickPost(entity);
    }
}
