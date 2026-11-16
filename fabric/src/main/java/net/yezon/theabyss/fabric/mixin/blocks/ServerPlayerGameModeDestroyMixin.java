package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.block.hooks.PlayerDestroyListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeDestroyMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean theabyss$afterPlayerDestroy(ServerLevel level, BlockPos pos, boolean movedByPiston, Operation<Boolean> original, @Local(ordinal = 0) BlockState adjustedState) {
        if (!(adjustedState.getBlock() instanceof PlayerDestroyListener listener)) {
            return original.call(level, pos, movedByPiston);
        }
        FluidState fluid = level.getFluidState(pos);
        ItemStack tool = this.player.getMainHandItem().copy();
        boolean willHarvest = !this.player.preventsBlockDrops() && this.player.hasCorrectToolForDrops(adjustedState);
        boolean removed = original.call(level, pos, movedByPiston);
        listener.afterDestroyedByPlayer(adjustedState, level, pos, this.player, tool, willHarvest, fluid);
        return removed;
    }
}
