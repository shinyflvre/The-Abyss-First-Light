package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import java.util.stream.Stream;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.fabric.fluids.OwnFluidEntity;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityFluidMixin implements OwnFluidEntity {
    @Unique
    private final Object2DoubleArrayMap<FluidType> theabyss$fluidHeights = new Object2DoubleArrayMap<>(2);

    @Unique
    private @Nullable FluidType theabyss$eyeFluid;

    @Override
    public Object2DoubleArrayMap<FluidType> theabyss$fluidHeights() {
        return this.theabyss$fluidHeights;
    }

    @Override
    public @Nullable FluidType theabyss$eyeFluid() {
        return this.theabyss$eyeFluid;
    }

    @Override
    public void theabyss$setEyeFluid(@Nullable FluidType type) {
        this.theabyss$eyeFluid = type;
    }

    @WrapOperation(
        method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean theabyss$ownFluidsAreNotVanillaFluids(FluidState state, TagKey<Fluid> tag, Operation<Boolean> original) {
        return FluidContact.typeOf(state) == null && original.call(state, tag);
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("HEAD"))
    private void theabyss$ownFluidHeights(CallbackInfoReturnable<Boolean> cir) {
        FluidContact.update((Entity)(Object)this);
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("RETURN"))
    private void theabyss$ownFluidFallAndFire(CallbackInfoReturnable<Boolean> cir) {
        FluidContact.afterUpdate((Entity)(Object)this);
    }

    @ModifyReturnValue(method = "updateInWaterStateAndDoFluidPushing", at = @At("RETURN"))
    private boolean theabyss$touchingOwnFluids(boolean original) {
        return original || FluidContact.touching((Entity)(Object)this);
    }

    @Inject(method = "updateFluidOnEyes", at = @At("HEAD"))
    private void theabyss$clearOwnEyeFluid(CallbackInfo ci) {
        FluidContact.clearEye((Entity)(Object)this);
    }

    @WrapOperation(
        method = "updateFluidOnEyes",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getTags()Ljava/util/stream/Stream;")
    )
    private Stream<TagKey<Fluid>> theabyss$ownEyeFluid(FluidState state, Operation<Stream<TagKey<Fluid>>> original) {
        if (FluidContact.typeOf(state) != null) {
            FluidContact.updateEye((Entity)(Object)this, state);
            return Stream.empty();
        }
        return original.call(state);
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void theabyss$swimInOwnFluids(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (!FluidContact.touching(self) && FluidContact.eye(self) == null) {
            return;
        }
        if (self.isSwimming()) {
            self.setSwimming(self.isSprinting() && (self.isInWater() || FluidContact.present(self, FluidType::canSwim)) && !self.isPassenger());
        } else {
            boolean water = self.isUnderWater() && self.level().getFluidState(self.blockPosition()).is(FluidTags.WATER);
            self.setSwimming(self.isSprinting() && (water || FluidContact.canStartSwimming(self)) && !self.isPassenger());
        }
        ci.cancel();
    }

    @ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
    private boolean theabyss$noCrawlInOwnFluids(boolean original) {
        return original && !FluidContact.present((Entity)(Object)this, FluidType::canSwim);
    }

    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
    private boolean theabyss$noSprintParticlesInOwnFluids(boolean original) {
        return original && !FluidContact.touching((Entity)(Object)this);
    }

    @ModifyExpressionValue(
        method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWaterRainOrBubble()Z")
    )
    private boolean theabyss$ownFluidsPutOutFire(boolean original) {
        return original || FluidContact.present((Entity)(Object)this, FluidType::canExtinguish);
    }
}
