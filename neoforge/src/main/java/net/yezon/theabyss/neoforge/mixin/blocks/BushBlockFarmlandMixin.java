package net.yezon.theabyss.neoforge.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.BushBlock;
import net.yezon.theabyss.block.AbyssFarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BushBlock.class)
public abstract class BushBlockFarmlandMixin {
    @WrapOperation(
        method = "mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z",
        at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/level/block/FarmBlock")
    )
    private boolean theabyss$abyssFarmlandSupportsNoVegetation(Object block, Operation<Boolean> original) {
        return original.call(block) && !(block instanceof AbyssFarmlandBlock);
    }
}
