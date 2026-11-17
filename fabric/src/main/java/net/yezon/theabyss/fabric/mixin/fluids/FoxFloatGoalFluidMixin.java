package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.animal.Fox;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxFloatGoal")
public abstract class FoxFloatGoalFluidMixin {
    @Shadow
    @Final
    Fox field_17976;

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean theabyss$foxFloatsInOwnFluids(boolean original) {
        return original || FluidContact.height(this.field_17976, FluidType::canSwim) > 0.25;
    }
}
