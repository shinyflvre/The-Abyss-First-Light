package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.CurseOnPotionActiveTickEvents;

public class CurseMobEffect extends ProcedureMobEffect {
    public CurseMobEffect() {
        super(MobEffectCategory.HARMFUL, -16777216);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        CurseOnPotionActiveTickEvents.execute(entity.level(), entity);
    }
}
