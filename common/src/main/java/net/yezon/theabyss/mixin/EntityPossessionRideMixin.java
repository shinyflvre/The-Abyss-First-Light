package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.yezon.theabyss.possession.PossessionRide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityPossessionRideMixin {
    @WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
        at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
    private void theabyss$possessionRideTriggers(Stream<Entity> passengers, Consumer<? super Entity> trigger, Operation<Void> original) {
        if (!PossessionRide.isSilent((Entity)(Object)this)) {
            original.call(passengers, trigger);
        }
    }

    @WrapOperation(method = "addPassenger(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V"))
    private void theabyss$possessionRideEvent(Entity vehicle, Holder<GameEvent> event, Entity passenger, Operation<Void> original) {
        if (!PossessionRide.isSilent(passenger)) {
            original.call(vehicle, event, passenger);
        }
    }
}
