package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.FearPotionStartedappliedEvents;

public class FearMobEffect extends ProcedureMobEffect {
    public FearMobEffect() {
        super(MobEffectCategory.HARMFUL, -16777165);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        FearPotionStartedappliedEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }
}
