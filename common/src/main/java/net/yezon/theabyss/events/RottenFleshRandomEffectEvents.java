package net.yezon.theabyss.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class RottenFleshRandomEffectEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (Math.random() < 0.5) {
                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                    _entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                }

                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                    _entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
                }
            }
        }
    }
}
