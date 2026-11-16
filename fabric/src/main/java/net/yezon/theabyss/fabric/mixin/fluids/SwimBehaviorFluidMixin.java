package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Swim.class)
public abstract class SwimBehaviorFluidMixin {
    @ModifyReturnValue(
        method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z",
        at = @At("RETURN")
    )
    private boolean theabyss$swimInOwnFluids(boolean original, ServerLevel level, Mob body) {
        return original || FluidContact.height(body, FluidType::canSwim) > body.getFluidJumpThreshold();
    }
}
