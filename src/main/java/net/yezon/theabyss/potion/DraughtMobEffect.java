package net.yezon.theabyss.potion;

import java.util.function.Consumer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DraughtMobEffect extends ProcedureMobEffect {
    private final Consumer<LivingEntity> ended;

    public DraughtMobEffect(int color, Consumer<LivingEntity> ended) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.ended = ended;
    }

    public DraughtMobEffect(int color) {
        this(color, entity -> {});
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return false;
    }

    @Override
    protected void onEnded(LivingEntity entity) {
        this.ended.accept(entity);
    }
}
