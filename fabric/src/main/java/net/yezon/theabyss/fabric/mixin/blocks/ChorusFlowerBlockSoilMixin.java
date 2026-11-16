package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChorusFlowerBlock.class)
public abstract class ChorusFlowerBlockSoilMixin {
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean theabyss$modSoilBelow(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockState state,
                                          @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        BlockPos soilPos = pos.below();
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, Direction.UP, state), original.call(soil, tag));
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1))
    private boolean theabyss$modSoilUnderPillar(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockState state,
                                                @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 1) int height) {
        BlockPos soilPos = pos.below(height + 1);
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, Direction.UP, state), original.call(soil, tag));
    }
}
