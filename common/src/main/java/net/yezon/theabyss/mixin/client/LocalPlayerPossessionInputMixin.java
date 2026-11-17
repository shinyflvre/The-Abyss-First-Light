package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.yezon.theabyss.possession.PossessionData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerPossessionInputMixin {
    @WrapOperation(method = "tick()V",
        at = @At(value = "NEW", target = "(FFZZ)Lnet/minecraft/network/protocol/game/ServerboundPlayerInputPacket;"))
    private ServerboundPlayerInputPacket theabyss$possessionInput(float xxa, float zza, boolean jumping, boolean sneaking,
        Operation<ServerboundPlayerInputPacket> original) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (PossessionData.of(player).active()) {
            return original.call(player.input.leftImpulse, player.input.forwardImpulse, jumping, sneaking);
        }
        return original.call(xxa, zza, jumping, sneaking);
    }
}
