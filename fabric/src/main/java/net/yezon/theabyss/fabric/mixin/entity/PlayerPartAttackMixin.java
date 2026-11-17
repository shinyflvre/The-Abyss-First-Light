package net.yezon.theabyss.fabric.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.entity.FabricPartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerPartAttackMixin {
    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private Entity theabyss$partHurtTarget(Entity hurtTarget) {
        return hurtTarget instanceof FabricPartEntity part ? part.getParent() : hurtTarget;
    }
}
