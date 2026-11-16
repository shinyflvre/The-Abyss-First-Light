package net.yezon.theabyss.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.init.TheabyssModMobEffects;

public class ApplyNodeEffectEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(new MobEffectInstance(TheabyssModMobEffects.NODE_POTION, 200, 0));
            }
        }
    }
}
