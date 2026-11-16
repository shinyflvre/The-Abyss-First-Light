package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.hooks.FireSpreadingBlock;
import net.yezon.theabyss.block.hooks.FlammableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @ModifyReturnValue(method = "getBurnOdds", at = @At("RETURN"))
    private int theabyss$modBurnOdds(int original, @Local(argsOnly = true) BlockState state) {
        if (state.getBlock() instanceof FlammableBlock block) {
            return block.getFlammability(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
        }
        return original;
    }

    @ModifyReturnValue(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("RETURN"))
    private int theabyss$modIgniteOdds(int original, @Local(argsOnly = true) BlockState state) {
        if (state.getBlock() instanceof FireSpreadingBlock block) {
            return block.getFireSpreadSpeed(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
        }
        return original;
    }
}
