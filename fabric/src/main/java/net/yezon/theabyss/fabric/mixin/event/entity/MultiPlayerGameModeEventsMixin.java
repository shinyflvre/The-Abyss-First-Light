package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.apache.commons.lang3.mutable.MutableObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeEventsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean theabyss$startCanceled;

    @WrapOperation(method = {"method_41936", "method_41935"}, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean theabyss$creativeLeftClick(MultiPlayerGameMode gameMode, BlockPos pos, Operation<Boolean> original) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && PlayerFabricEvents.leftClickBlock(player, pos)) {
            return false;
        }
        return original.call(gameMode, pos);
    }

    @Inject(method = "startDestroyBlock", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 1))
    private void theabyss$leftClick(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = this.minecraft.player;
        this.theabyss$startCanceled = player != null && PlayerFabricEvents.leftClickBlock(player, pos);
    }

    @WrapMethod(method = "method_41930")
    private Packet<?> theabyss$leftClickPrediction(BlockState state, BlockPos pos, Direction direction, int sequence, Operation<Packet<?>> original) {
        if (this.theabyss$startCanceled) {
            this.theabyss$startCanceled = false;
            return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
        }
        return original.call(state, pos, direction, sequence);
    }

    @Inject(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyProgress:F",
        opcode = Opcodes.GETFIELD, ordinal = 2), cancellable = true)
    private void theabyss$leftClickHold(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && PlayerFabricEvents.leftClickBlock(player, pos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "performUseItemOn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;localPlayerMode:Lnet/minecraft/world/level/GameType;",
        opcode = Opcodes.GETFIELD, ordinal = 0), cancellable = true)
    private void theabyss$rightClickBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir,
        @Share("theabyssRightClick") LocalRef<PlayerFabricEvents.RightClick> click) {
        PlayerFabricEvents.RightClick result = PlayerFabricEvents.rightClickBlock(player, hand, hit.getBlockPos());
        click.set(result);
        if (result.canceled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 2), cancellable = true)
    private void theabyss$rightClickBlockItem(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir,
        @Share("theabyssRightClick") LocalRef<PlayerFabricEvents.RightClick> click) {
        PlayerFabricEvents.RightClick result = click.get();
        if (result != null && result.useItem() == TriState.FALSE) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "method_41929", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"),
        cancellable = true)
    private void theabyss$rightClickItem(InteractionHand hand, Player player, MutableObject<InteractionResult> interactionResult, int sequence,
        CallbackInfoReturnable<Packet<?>> cir, @Local ServerboundUseItemPacket packet) {
        if (PlayerFabricEvents.rightClickItem(player, hand)) {
            interactionResult.setValue(InteractionResult.PASS);
            cir.setReturnValue(packet);
        }
    }
}
