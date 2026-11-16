package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import net.yezon.theabyss.platform.client.event.InputEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientEventsMixin {
    @Shadow
    private boolean gameLoadFinished;

    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Shadow
    public LocalPlayer player;

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;clientTickCount:J", opcode = 181, shift = At.Shift.AFTER))
    private void theabyss$clientTickPre(CallbackInfo ci) {
        if (this.gameLoadFinished) {
            ClientFabricEvents.clientTickPre();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void theabyss$clientTickPost(CallbackInfo ci) {
        if (this.gameLoadFinished) {
            ClientFabricEvents.clientTickPost();
        }
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render()V"))
    private void theabyss$renderFramePre(boolean advanceGameTime, CallbackInfo ci) {
        ClientFabricEvents.renderFramePre(this.deltaTracker);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER))
    private void theabyss$loggingOut(CallbackInfo ci) {
        ClientFabricEvents.loggingOut();
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getAttackAnimation()Lnet/minecraft/world/item/component/SwingAnimation;",
        shift = At.Shift.AFTER), cancellable = true)
    private void theabyss$attackKey(CallbackInfoReturnable<Boolean> cir, @Local ItemStack heldItem) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(0, InteractionHand.MAIN_HAND);
        if (event != null && event.isCanceled()) {
            if (event.getSwingHand()) {
                this.player.swing(InteractionHand.MAIN_HAND, heldItem.getAttackAnimation(), false);
            }
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "continueAttack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean theabyss$continueAttackKey(MultiPlayerGameMode gameMode, BlockPos pos, Direction direction, Operation<Boolean> original) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(0, InteractionHand.MAIN_HAND);
        if (event != null && event.isCanceled()) {
            return event.getSwingHand();
        }
        return original.call(gameMode, pos, direction);
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getInteractAnimation()Lnet/minecraft/world/item/component/SwingAnimation;",
        shift = At.Shift.AFTER), cancellable = true)
    private void theabyss$useKey(CallbackInfo ci, @Local InteractionHand hand, @Local ItemStack heldItem) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(1, hand);
        if (event != null && event.isCanceled()) {
            if (event.getSwingHand()) {
                this.player.swing(hand, heldItem.getInteractAnimation(), false);
            }
            ci.cancel();
        }
    }

    @Inject(method = "pickBlockOrEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"), cancellable = true)
    private void theabyss$pickKey(CallbackInfo ci) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(2, InteractionHand.MAIN_HAND);
        if (event != null && event.isCanceled()) {
            ci.cancel();
        }
    }
}
