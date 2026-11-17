package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.block.hooks.PlayerDestroyListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeDestroyMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean theabyss$afterPlayerDestroy(Level level, BlockPos pos, BlockState replacement, int flags, Operation<Boolean> original,
                                                @Local(ordinal = 0) BlockState oldState, @Local FluidState fluid) {
        if (!(oldState.getBlock() instanceof PlayerDestroyListener listener) || this.minecraft.player == null) {
            return original.call(level, pos, replacement, flags);
        }
        boolean changed = original.call(level, pos, replacement, flags);
        listener.afterDestroyedByPlayer(oldState, level, pos, this.minecraft.player, false, fluid);
        return changed;
    }
}
