package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityEquipment.class)
public abstract class EquipmentDropCaptureMixin {
    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean theabyss$captureEquipmentDrop(Level level, Entity drop, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity dropper) {
        if (drop instanceof ItemEntity item && LivingFabricEvents.captureDrop(dropper, item)) {
            return true;
        }
        return original.call(level, drop);
    }
}
