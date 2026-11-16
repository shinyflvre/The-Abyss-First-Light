package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FloatGoal.class)
public abstract class FloatGoalFluidMixin {
    @Shadow
    @Final
    private Mob mob;

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean theabyss$floatInOwnFluids(boolean original) {
        return original || FluidContact.height(this.mob, FluidType::canSwim) > this.mob.getFluidJumpThreshold();
    }
}
