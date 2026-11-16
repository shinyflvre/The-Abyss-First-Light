package net.yezon.theabyss.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.events.NodeEffectEvents;
import net.yezon.theabyss.events.NodeParticlesEvents;

public class NodePotionMobEffect extends ProcedureMobEffect {
    public NodePotionMobEffect() {
        super(MobEffectCategory.NEUTRAL, -65281);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        NodeEffectEvents.execute(entity);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        NodeParticlesEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
    }
}
