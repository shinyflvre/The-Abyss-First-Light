package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.item.AbyssTotemProtection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTotemMixin {
    @WrapMethod(method = "checkTotemDeathProtection")
    private boolean theabyss$abyssTotems(DamageSource source, Operation<Boolean> original) {
        return AbyssTotemProtection.check((LivingEntity)(Object)this, source, AbyssTotemProtection.ALWAYS, () -> original.call(source));
    }
}
