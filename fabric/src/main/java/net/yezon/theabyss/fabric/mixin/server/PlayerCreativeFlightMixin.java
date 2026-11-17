package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.server.FabricAttributes;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerCreativeFlightMixin {
    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder theabyss$addCreativeFlight(AttributeSupplier.Builder builder) {
        return builder.add(FabricAttributes.CREATIVE_FLIGHT);
    }

    @WrapOperation(method = "causeFallDamage",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    private boolean theabyss$mayFly(Abilities abilities, Operation<Boolean> original) {
        return original.call(abilities) || FabricAttributes.attributeFlight((Player)(Object)this);
    }
}
