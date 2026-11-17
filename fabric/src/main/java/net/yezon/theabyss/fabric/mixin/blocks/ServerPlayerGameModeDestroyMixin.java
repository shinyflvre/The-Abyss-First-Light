package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState theabyss$captureAdjustedState(Block block, Level level, BlockPos pos, BlockState state, Player player, Operation<BlockState> original,
                                                     @Share("adjustedState") LocalRef<BlockState> adjustedState) {
        BlockState adjusted = original.call(block, level, pos, state, player);
        adjustedState.set(adjusted);
        return adjusted;
    }

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean theabyss$afterPlayerDestroy(ServerLevel level, BlockPos pos, boolean movedByPiston, Operation<Boolean> original,
                                                @Share("adjustedState") LocalRef<BlockState> adjustedState) {
        BlockState state = adjustedState.get();
        if (state == null || !(state.getBlock() instanceof PlayerDestroyListener listener)) {
            return original.call(level, pos, movedByPiston);
        }
        FluidState fluid = level.getFluidState(pos);
        boolean willHarvest = !((ServerPlayerGameMode)(Object)this).isCreative() && this.player.hasCorrectToolForDrops(state);
        boolean removed = original.call(level, pos, movedByPiston);
        listener.afterDestroyedByPlayer(state, level, pos, this.player, willHarvest, fluid);
        return removed;
    }
}
