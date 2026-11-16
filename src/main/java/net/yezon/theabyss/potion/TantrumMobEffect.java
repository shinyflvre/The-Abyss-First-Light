package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.TantrumOnPotionEvents;

public class TantrumMobEffect extends ProcedureMobEffect {
    public TantrumMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -16711785);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        TantrumOnPotionEvents.execute(entity);
    }
}
