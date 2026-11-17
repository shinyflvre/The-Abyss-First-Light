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
    @Final
    private DeltaTracker.Timer timer;

    @Shadow
    public LocalPlayer player;

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;clientTickCount:J", opcode = 181, shift = At.Shift.AFTER))
    private void theabyss$clientTickPre(CallbackInfo ci) {
        ClientFabricEvents.clientTickPre();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void theabyss$clientTickPost(CallbackInfo ci) {
        ClientFabricEvents.clientTickPost();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void theabyss$renderFramePre(boolean renderLevel, CallbackInfo ci) {
        ClientFabricEvents.renderFramePre(this.timer);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER))
    private void theabyss$loggingOut(CallbackInfo ci) {
        ClientFabricEvents.loggingOut();
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;",
        ordinal = 0), cancellable = true)
    private void theabyss$attackKey(CallbackInfoReturnable<Boolean> cir) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(0, InteractionHand.MAIN_HAND);
        if (event != null && event.isCanceled()) {
            if (event.getSwingHand()) {
                this.player.swing(InteractionHand.MAIN_HAND);
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

    @Inject(method = "startUseItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0),
        cancellable = true)
    private void theabyss$useKey(CallbackInfo ci, @Local InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(1, hand);
        if (event != null && event.isCanceled()) {
            if (event.getSwingHand()) {
                this.player.swing(hand);
            }
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;",
        ordinal = 0), cancellable = true)
    private void theabyss$pickKey(CallbackInfo ci) {
        InputEvent.InteractionKeyMappingTriggered event = ClientFabricEvents.interaction(2, InteractionHand.MAIN_HAND);
        if (event != null && event.isCanceled()) {
            ci.cancel();
        }
    }
}
