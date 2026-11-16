package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.GhostOnPotionActiveEvents;
import net.yezon.theabyss.events.GhostPotionEndEvents;
import net.yezon.theabyss.events.GhostPotionStartEvents;

public class GhostMobEffect extends ProcedureMobEffect {
    public GhostMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -2500135);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        GhostPotionStartEvents.execute(entity);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        GhostOnPotionActiveEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
    }

    @Override
    protected void onEnded(LivingEntity entity) {
        GhostPotionEndEvents.execute(entity);
    }
}
