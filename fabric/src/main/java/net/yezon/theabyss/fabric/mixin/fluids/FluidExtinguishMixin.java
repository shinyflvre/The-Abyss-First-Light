package net.yezon.theabyss.fabric.mixin.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluid.class)
public abstract class FluidExtinguishMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void theabyss$ownFluidsExtinguish(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, CallbackInfo ci) {
        FluidType type = FluidContact.typeOf((Fluid)(Object)this);
        if (type != null && type.canExtinguish()) {
            effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
        }
    }
}
