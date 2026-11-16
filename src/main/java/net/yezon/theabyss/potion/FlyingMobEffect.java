package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.FlyingOnPotionActiveEvents;

public class FlyingMobEffect extends ProcedureMobEffect {
    public FlyingMobEffect() {
        super(MobEffectCategory.NEUTRAL, -10027060);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        FlyingOnPotionActiveEvents.execute(entity);
    }
}
