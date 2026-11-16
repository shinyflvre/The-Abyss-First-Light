package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.AfterLifeHandlerEvents;

public class AfterLifeMobEffect extends ProcedureMobEffect {
    public AfterLifeMobEffect() {
        super(MobEffectCategory.NEUTRAL, -10092442);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        AfterLifeHandlerEvents.started(entity);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        AfterLifeHandlerEvents.tick(entity);
    }

    @Override
    protected void onEnded(LivingEntity entity) {
        AfterLifeHandlerEvents.ended(entity);
    }
}
