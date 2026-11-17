package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.cave.AbyssPointedDripstoneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneSpeleothemMixin {
    @WrapOperation(
        method = "calculateDripstoneThickness(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/DripstoneThickness;",
        at = {
            @At(value = "INVOKE", ordinal = 1,
                target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;isPointedDripstoneWithDirection(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"),
            @At(value = "INVOKE", ordinal = 2,
                target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;isPointedDripstoneWithDirection(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z")
        }
    )
    private static boolean theabyss$speleothemNeighbour(BlockState state, Direction direction, Operation<Boolean> original) {
        return original.call(state, direction) || AbyssPointedDripstoneBlock.isSpeleothemWithDirection(state, direction);
    }

    @WrapOperation(
        method = "spawnFallingStalactite(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;isStalactite(Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private static boolean theabyss$fallingSpeleothem(BlockState state, Operation<Boolean> original) {
        return original.call(state) || AbyssPointedDripstoneBlock.isSpeleothemWithDirection(state, Direction.DOWN);
    }

    @WrapOperation(
        method = "spawnFallingStalactite(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;isTip(Lnet/minecraft/world/level/block/state/BlockState;Z)Z")
    )
    private static boolean theabyss$fallingSpeleothemTip(BlockState state, boolean includeMergedTip, Operation<Boolean> original) {
        return original.call(state, includeMergedTip) || AbyssPointedDripstoneBlock.isSpeleothemTip(state, includeMergedTip);
    }

    @WrapOperation(
        method = "findTip(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;findBlockVertical(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$AxisDirection;Ljava/util/function/BiPredicate;Ljava/util/function/Predicate;I)Ljava/util/Optional;")
    )
    private static Optional<BlockPos> theabyss$speleothemTipTarget(LevelAccessor level, BlockPos pos, Direction.AxisDirection axisDirection,
                                                                    BiPredicate<BlockPos, BlockState> pathPredicate, Predicate<BlockState> targetPredicate,
                                                                    int maxSteps, Operation<Optional<BlockPos>> original,
                                                                    @Local(argsOnly = true) boolean includeMergedTip) {
        return original.call(level, pos, axisDirection, pathPredicate,
            targetPredicate.or(state -> AbyssPointedDripstoneBlock.isSpeleothemTip(state, includeMergedTip)), maxSteps);
    }

    @WrapOperation(
        method = "growStalactiteOrStalagmiteIfPossible(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;canDrip(Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private static boolean theabyss$growingSpeleothemTip(BlockState state, Operation<Boolean> original) {
        return original.call(state) || AbyssPointedDripstoneBlock.isFreeHangingStalactite(state);
    }

    @Inject(
        method = "spawnDripParticle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("HEAD"), cancellable = true
    )
    private static void theabyss$abyssDripParticle(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof AbyssPointedDripstoneBlock) {
            AbyssPointedDripstoneBlock.spawnDripParticle(level, pos, state);
            ci.cancel();
        }
    }
}
