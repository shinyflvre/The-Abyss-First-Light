package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFluidMixin {
    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void theabyss$travelInOwnFluids(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!self.isControlledByLocalInstance() || self.isInWater()) {
            return;
        }
        FluidState state = self.level().getFluidState(self.blockPosition());
        FluidType type = FluidContact.typeOf(state);
        if (type == null || FluidContact.height(self, type) <= 0.0 || !this.isAffectedByFluids() || self.canStandOnFluid(state)) {
            return;
        }
        double gravity = self.getGravity();
        if (self.getDeltaMovement().y <= 0.0 && self.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = Math.min(gravity, 0.01);
        }
        if (type.move(self, input, gravity)) {
            self.calculateEntityAnimation(self instanceof FlyingAnimal);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D", ordinal = 1)
    )
    private double theabyss$jumpHeightInOwnFluids(double original) {
        LivingEntity self = (LivingEntity)(Object)this;
        double own = FluidContact.highest(self);
        return own > original && !self.isInLava() ? own : original;
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
        return FluidContact.highest(self) > 0.0 && !self.isInLava();
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

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWaterRainOrBubble()Z")
    )
    private boolean theabyss$ownFluidsExtinguish(boolean original) {
        return original || FluidContact.present((LivingEntity)(Object)this, FluidType::canExtinguish);
    }
}
