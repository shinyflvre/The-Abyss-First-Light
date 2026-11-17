package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Abilities;
import net.yezon.theabyss.fabric.server.FabricAttributes;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerCreativeFlightMixin {
    @WrapOperation(method = {"handleMovePlayer", "handlePlayerAbilities"},
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    private boolean theabyss$mayFly(Abilities abilities, Operation<Boolean> original) {
        return original.call(abilities) || FabricAttributes.attributeFlight(((ServerGamePacketListenerImpl)(Object)this).player);
    }
}
