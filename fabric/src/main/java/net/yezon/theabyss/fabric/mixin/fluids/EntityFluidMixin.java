package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityFluidMixin {
    @ModifyReturnValue(method = "isInLava", at = @At("RETURN"))
    private boolean theabyss$ownFluidsAreNotLava(boolean original) {
        return original && !FluidContact.lavaOnlyFromOwnFluids((Entity)(Object)this);
    }

    @WrapOperation(
        method = "updateFluidInteraction",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;applyCurrentTo(Lnet/minecraft/tags/TagKey;Lnet/minecraft/world/entity/Entity;D)V")
    )
    private void theabyss$noLavaCurrentFromOwnFluids(EntityFluidInteraction interaction, TagKey<Fluid> fluid, Entity entity, double scale, Operation<Void> original) {
        if (fluid == FluidTags.LAVA && FluidContact.lavaOnlyFromOwnFluids(entity)) {
            return;
        }
        original.call(interaction, fluid, entity, scale);
    }

    @Inject(method = "updateFluidInteraction", at = @At("RETURN"))
    private void theabyss$ownFluidCurrents(CallbackInfoReturnable<Boolean> cir) {
        FluidContact.pushByCurrents((Entity)(Object)this);
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;checkBelowWorld()V"))
    private void theabyss$ownFluidFallDistance(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        float modifier = FluidContact.fallDistanceModifier(self);
        if (modifier < 1.0F && !self.isInLava() && !self.isInWater()) {
            self.fallDistance *= modifier;
        }
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void theabyss$swimInOwnFluids(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (!FluidContact.touching(self)) {
            return;
        }
        if (self.isSwimming()) {
            self.setSwimming(self.isSprinting() && (self.isInWater() || FluidContact.height(self, FluidType::canSwim) > 0.0) && !self.isPassenger());
        } else {
            self.setSwimming(self.isSprinting() && FluidContact.canStartSwimming(self) && !self.isPassenger());
        }
        ci.cancel();
    }

    @ModifyReturnValue(method = "isInShallowWater", at = @At("RETURN"))
    private boolean theabyss$shallowOwnFluids(boolean original) {
        Entity self = (Entity)(Object)this;
        return original || FluidContact.height(self, FluidType::canSwim) > 0.0 && !FluidContact.canStartSwimming(self);
    }

    @ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
    private boolean theabyss$noCrawlInOwnFluids(boolean original) {
        return original && FluidContact.height((Entity)(Object)this, FluidType::canSwim) <= 0.0;
    }

    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
    private boolean theabyss$noSprintParticlesInOwnFluids(boolean original) {
        return original && !FluidContact.touching((Entity)(Object)this);
    }
}
