package net.yezon.theabyss.events;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.network.TheabyssModVariables;

public class IncorytheArmorAbilityEvents {
    public static void execute(Entity entity) {
        if (entity != null) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                _entity.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        60,
                        (int)(
                            0.0
                                + TheabyssModVariables.getPlayerVariables(entity)
                                    .ArmorPotionLevel
                        ),
                        false,
                        false
                    )
                );
            }
        }
    }
}
