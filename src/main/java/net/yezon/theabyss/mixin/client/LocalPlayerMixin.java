package net.yezon.theabyss.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.yezon.theabyss.gravity.GravityEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "updateAutoJump", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityNoAutoJump(float xa, float za, CallbackInfo ci) {
        if (((GravityEntity)this).theabyss$gravityId() != 0) {
            ci.cancel();
        }
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityNoUprightPush(double x, double z, CallbackInfo ci) {
        if (((GravityEntity)this).theabyss$gravityId() != 0) {
            ci.cancel();
        }
    }
}
