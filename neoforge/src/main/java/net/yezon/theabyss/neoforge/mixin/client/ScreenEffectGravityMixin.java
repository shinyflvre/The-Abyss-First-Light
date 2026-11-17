package net.yezon.theabyss.neoforge.mixin.client;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.client.gravity.GravityViewBlocking;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectGravityMixin {
    @Inject(method = "getOverlayBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void theabyss$gravityViewBlocking(Player player, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        if (!GravityViewBlocking.applies(player)) {
            return;
        }
        cir.setReturnValue(GravityViewBlocking.find(player));
    }
}
