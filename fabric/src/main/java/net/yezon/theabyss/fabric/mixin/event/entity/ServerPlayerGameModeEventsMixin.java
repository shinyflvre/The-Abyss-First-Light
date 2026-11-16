package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeEventsMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    private GameType gameModeForPlayer;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void theabyss$leftClickBlock(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, CallbackInfo ci) {
        if (PlayerFabricEvents.leftClickBlock(this.player, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void theabyss$breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (LevelFabricEvents.breakBlock(this.level, this.gameModeForPlayer, this.player, pos, this.level.getBlockState(pos))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "useItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0), cancellable = true)
    private void theabyss$rightClickItem(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (PlayerFabricEvents.rightClickItem(player, hand)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;",
        opcode = Opcodes.GETFIELD, ordinal = 0), cancellable = true)
    private void theabyss$rightClickBlock(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
        CallbackInfoReturnable<InteractionResult> cir, @Share("theabyssRightClick") LocalRef<PlayerFabricEvents.RightClick> click) {
        PlayerFabricEvents.RightClick result = PlayerFabricEvents.rightClickBlock(player, hand, hit.getBlockPos());
        click.set(result);
        if (result.canceled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 2), cancellable = true)
    private void theabyss$rightClickBlockItem(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
        CallbackInfoReturnable<InteractionResult> cir, @Share("theabyssRightClick") LocalRef<PlayerFabricEvents.RightClick> click) {
        PlayerFabricEvents.RightClick result = click.get();
        if (result != null && result.useItem() == TriState.FALSE) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
