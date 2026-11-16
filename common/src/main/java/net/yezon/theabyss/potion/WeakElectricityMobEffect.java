package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.WeakElectricityOnEffectActiveEvents;

public class WeakElectricityMobEffect extends ProcedureMobEffect {
    public WeakElectricityMobEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        WeakElectricityOnEffectActiveEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }
}
