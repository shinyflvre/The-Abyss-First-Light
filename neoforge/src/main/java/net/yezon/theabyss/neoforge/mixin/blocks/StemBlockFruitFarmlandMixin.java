package net.yezon.theabyss.neoforge.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.StemBlock;
import net.yezon.theabyss.block.AbyssFarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StemBlock.class)
public abstract class StemBlockFruitFarmlandMixin {
    @WrapOperation(
        method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
        at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/level/block/FarmBlock")
    )
    private boolean theabyss$noFruitOnAbyssFarmland(Object block, Operation<Boolean> original) {
        return original.call(block) && !(block instanceof AbyssFarmlandBlock);
    }
}
