package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.ElectricityOnEffectActiveEvents;

public class ElectricityMobEffect extends ProcedureMobEffect {
    public ElectricityMobEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        ElectricityOnEffectActiveEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }
}
