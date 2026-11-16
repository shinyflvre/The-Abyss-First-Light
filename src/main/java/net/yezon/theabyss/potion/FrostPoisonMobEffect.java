package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.FrostPoisonOActiveEvents;

public class FrostPoisonMobEffect extends ProcedureMobEffect {
    public FrostPoisonMobEffect() {
        super(MobEffectCategory.NEUTRAL, -3342337);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        FrostPoisonOActiveEvents.execute(entity);
    }
}
