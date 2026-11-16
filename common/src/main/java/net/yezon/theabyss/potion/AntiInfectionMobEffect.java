package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.AntiInfectionTickEvents;

public class AntiInfectionMobEffect extends ProcedureMobEffect {
    public AntiInfectionMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        AntiInfectionTickEvents.execute(entity);
    }
}
