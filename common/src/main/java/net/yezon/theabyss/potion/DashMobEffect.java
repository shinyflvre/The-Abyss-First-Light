package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.DashEffectActiveEvents;

public class DashMobEffect extends ProcedureMobEffect {
    public DashMobEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        DashEffectActiveEvents.execute(entity);
    }
}
