package net.yezon.theabyss.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AnimaFireEffectEvents {
    public static void execute(Entity entity) {
        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 1));
            _entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 1));
            _entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 1));
        }
    }
}
