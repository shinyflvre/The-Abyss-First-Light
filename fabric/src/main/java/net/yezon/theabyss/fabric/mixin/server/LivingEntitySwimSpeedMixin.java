package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.server.FabricAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwimSpeedMixin {
    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder theabyss$addSwimSpeed(AttributeSupplier.Builder builder) {
        return builder.add(FabricAttributes.SWIM_SPEED);
    }

    @WrapOperation(method = "travelInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"))
    private void theabyss$swimSpeed(LivingEntity entity, float speed, Vec3 input, Operation<Void> original) {
        original.call(entity, speed * (float)FabricAttributes.swimSpeed(entity), input);
    }

    @WrapOperation(method = "jumpInLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 theabyss$swimUp(Vec3 movement, double x, double y, double z, Operation<Vec3> original, @Local(argsOnly = true) TagKey<Fluid> type) {
        double factor = type == FluidTags.WATER ? FabricAttributes.swimSpeed((LivingEntity)(Object)this) : 1.0;
        return original.call(movement, x, y * factor, z);
    }

    @WrapOperation(method = "goDownInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 theabyss$swimDown(Vec3 movement, double x, double y, double z, Operation<Vec3> original) {
        return original.call(movement, x, y * FabricAttributes.swimSpeed((LivingEntity)(Object)this), z);
    }
}
