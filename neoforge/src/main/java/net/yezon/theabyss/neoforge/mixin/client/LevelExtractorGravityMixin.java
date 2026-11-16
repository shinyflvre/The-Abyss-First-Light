package net.yezon.theabyss.neoforge.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.client.gravity.GravityViewBlocking;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorGravityMixin {
    @Inject(method = "getViewBlockingStateAndPos", at = @At("HEAD"), cancellable = true)
    private static void theabyss$gravityViewBlocking(LocalPlayer player, Frustum frustum, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        if (!GravityViewBlocking.applies(player)) {
            return;
        }
        cir.setReturnValue(GravityViewBlocking.find(player, frustum));
    }
}
