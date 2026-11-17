package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.yezon.theabyss.block.cave.AbyssPointedDripstoneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorSpeleothemMixin {
    @ModifyReturnValue(
        method = "getPathTypeFromState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/pathfinder/PathType;",
        at = @At("RETURN")
    )
    private static PathType theabyss$speleothemsAreCautious(PathType original, @Local(argsOnly = true) BlockGetter level,
                                                            @Local(argsOnly = true) BlockPos pos) {
        if (original == PathType.DAMAGE_CAUTIOUS) {
            return original;
        }
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof AbyssPointedDripstoneBlock && AbyssPointedDripstoneBlock.isSpeleothem(state) ? PathType.DAMAGE_CAUTIOUS : original;
    }
}
