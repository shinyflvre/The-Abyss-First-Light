package net.yezon.theabyss.fabric.mixin.event.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.yezon.theabyss.fabric.event.FabricDamageState;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnderDragon.class)
public abstract class EnderDragonExperienceMixin {
    @ModifyArg(method = "tickDeath", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), index = 2)
    private int theabyss$experienceDrop(int experience) {
        LivingEntity self = (LivingEntity)(Object)this;
        return LivingFabricEvents.experience(self, ((FabricDamageState)self).theabyss$lastHurtByPlayer(), experience);
    }
}
