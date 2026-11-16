package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelEventsMixin {
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

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void theabyss$vanillaGameEvent(Holder<GameEvent> gameEvent, Vec3 position, GameEvent.Context context, CallbackInfo ci) {
        if (ServerFabricEvents.vanillaGameEvent(context.sourceEntity())) {
            ci.cancel();
        }
    }
}
