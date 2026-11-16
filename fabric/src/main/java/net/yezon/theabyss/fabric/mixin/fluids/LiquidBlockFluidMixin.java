package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockFluidMixin {
    @Shadow
    @Final
    protected FlowingFluid fluid;

    @WrapOperation(
        method = "neighborChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean theabyss$noNeighbourReactionForOwnFluids(LiquidBlock block, Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        if (FluidContact.typeOf(this.fluid) != null) {
            return true;
        }
        return original.call(block, level, pos, state);
    }
}
