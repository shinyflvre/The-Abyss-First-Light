package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.ImmortalPotionActiveEvents;
import net.yezon.theabyss.events.ImmortalPotionExpiresEvents;

public class ImmortalMobEffect extends ProcedureMobEffect {
    public ImmortalMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -16751002);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        ImmortalPotionActiveEvents.execute(entity);
    }

    @Override
    protected void onEnded(LivingEntity entity) {
        ImmortalPotionExpiresEvents.execute(entity);
    }
}
