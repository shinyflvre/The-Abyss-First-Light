package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChorusPlantBlock.class)
public abstract class ChorusPlantBlockSoilMixin {
    @WrapOperation(method = "getStateWithConnections", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean theabyss$modSoilConnection(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level,
                                                      @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState defaultState) {
        BlockPos soilPos = pos.below();
        return original.call(soil, tag) || FabricSoil.decision(soil, level, soilPos, Direction.UP, defaultState) == TriState.TRUE;
    }

    @WrapOperation(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean theabyss$modSoilShape(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) BlockState state,
                                          @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) Direction directionToNeighbour,
                                          @Local(argsOnly = true, ordinal = 1) BlockPos neighbourPos) {
        BlockPos soilPos = neighbourPos.relative(directionToNeighbour);
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, directionToNeighbour.getOpposite(), state), original.call(soil, tag));
    }

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1))
    private boolean theabyss$modSoilSurvive(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockState state,
                                            @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        BlockPos soilPos = pos.below();
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, Direction.UP, state), original.call(soil, tag));
    }
}
