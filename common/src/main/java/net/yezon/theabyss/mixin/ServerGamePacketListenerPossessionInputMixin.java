package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.yezon.theabyss.possession.PossessionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerPossessionInputMixin {
    @WrapOperation(method = "handlePlayerInput(Lnet/minecraft/network/protocol/game/ServerboundPlayerInputPacket;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setPlayerInput(FFZZ)V"))
    private void theabyss$possessionInput(ServerPlayer player, float xxa, float zza, boolean jumping, boolean sneaking, Operation<Void> original) {
        if (PossessionManager.captureInput(player, xxa, zza, jumping)) {
            original.call(player, 0.0F, 0.0F, false, sneaking);
        } else {
            original.call(player, xxa, zza, jumping, sneaking);
        }
    }
}
