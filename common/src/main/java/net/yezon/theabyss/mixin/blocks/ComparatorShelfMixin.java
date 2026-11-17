package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.wood.AbyssShelfBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorShelfMixin {
    @WrapOperation(
        method = "getInputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getAnalogOutputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I")
    )
    private int theabyss$shelfSignalFromBehind(BlockState target, Level level, BlockPos targetPos, Operation<Integer> original,
                                               @Local(argsOnly = true) BlockState comparator) {
        if (target.getBlock() instanceof AbyssShelfBlock shelf) {
            return shelf.getAnalogOutputSignal(target, level, targetPos, comparator.getValue(ComparatorBlock.FACING).getOpposite());
        }
        return original.call(target, level, targetPos);
    }
}
