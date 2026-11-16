package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.CursedAuraActiveEvents;

public class CursedAuraMobEffect extends ProcedureMobEffect {
    public CursedAuraMobEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        CursedAuraActiveEvents.execute(entity.level(), entity);
    }
}
