package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFluidMixin {
    @ModifyReturnValue(method = "shouldTravelInFluid", at = @At("RETURN"))
    private boolean theabyss$travelInOwnFluids(boolean original, FluidState fluidState) {
        if (original) {
            return true;
        }
        FluidType type = FluidContact.typeOf(fluidState);
        if (type == null) {
            return false;
        }
        LivingEntity self = (LivingEntity)(Object)this;
        return FluidContact.height(self, type) > 0.0 && self.isAffectedByFluids() && !self.canStandOnFluid(fluidState);
    }

    @WrapOperation(
        method = "travelInFluid",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travelInWater(Lnet/minecraft/world/phys/Vec3;DZD)V")
    )
    private void theabyss$moveInOwnFluidsBeforeWater(LivingEntity self, Vec3 input, double gravity, boolean falling, double oldY, Operation<Void> original) {
        if (!FluidContact.move(self, input, gravity)) {
            original.call(self, input, gravity, falling, oldY);
        }
    }

    @WrapOperation(
        method = "travelInFluid",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V")
    )
    private void theabyss$moveInOwnFluidsBeforeLava(LivingEntity self, Vec3 input, double gravity, boolean falling, double oldY, Operation<Void> original) {
        if (FluidContact.move(self, input, gravity)) {
            return;
        }
        if (FluidContact.touching(self) && !self.isInLava()) {
            return;
        }
        original.call(self, input, gravity, falling, oldY);
    }

    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D", ordinal = 1)
    )
    private double theabyss$jumpHeightInOwnFluids(double original) {
        LivingEntity self = (LivingEntity)(Object)this;
        double own = FluidContact.highestIfOwn(self);
        return own > 0.0 && !self.isInLava() ? own : original;
    }

    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z", ordinal = 0)
    )
    private boolean theabyss$jumpInOwnFluids(boolean original) {
        if (original) {
            return true;
        }
        LivingEntity self = (LivingEntity)(Object)this;
        return FluidContact.highestIfOwn(self) > 0.0 && !self.isInLava();
    }

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$breatheInOwnFluids(boolean original) {
        return original || FluidContact.eyeIn((LivingEntity)(Object)this, FluidType::canDrown);
    }

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canBreatheUnderwater()Z")
    )
    private boolean theabyss$noBreathingInOwnFluids(boolean original) {
        return original && !FluidContact.eyeIn((LivingEntity)(Object)this, FluidType::canDrown);
    }

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;dismountsUnderwater()Z")
    )
    private boolean theabyss$stayMountedInOwnFluids(boolean original) {
        return original && ((LivingEntity)(Object)this).isEyeInFluid(FluidTags.WATER);
    }
}
