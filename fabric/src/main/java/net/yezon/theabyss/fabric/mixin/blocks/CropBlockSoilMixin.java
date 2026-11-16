package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CropBlock.class)
public abstract class CropBlockSoilMixin {
    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean theabyss$modSoilGrowth(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level,
                                                  @Local(ordinal = 1) BlockPos below, @Local(ordinal = 0) int xx, @Local(ordinal = 1) int zz) {
        BlockPos soilPos = below.offset(xx, 0, zz);
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, Direction.UP, soil), original.call(soil, tag));
    }
}
