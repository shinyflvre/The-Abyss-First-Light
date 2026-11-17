package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.util.AbyssTeleport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerTeleportMixin {
    @WrapOperation(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(DDD)V"))
    private void theabyss$keepMotion(Player player, double x, double y, double z, Operation<Void> original,
        @Local(argsOnly = true) ClientboundPlayerPositionPacket packet) {
        if (AbyssTeleport.keepsMotion(packet)) {
            Vec3 motion = player.getDeltaMovement();
            original.call(player, motion.x, motion.y, motion.z);
            return;
        }
        original.call(player, x, y, z);
    }
}
