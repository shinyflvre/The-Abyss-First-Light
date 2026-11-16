package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.FirestormOnPotionActiveEvents;

public class FirestormMobEffect extends ProcedureMobEffect {
    public FirestormMobEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        FirestormOnPotionActiveEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }
}
