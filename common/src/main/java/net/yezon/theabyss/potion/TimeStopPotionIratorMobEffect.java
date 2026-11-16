package net.yezon.theabyss.potion;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.timestop.TimeStop;

public class TimeStopPotionIratorMobEffect extends ProcedureMobEffect {
    public TimeStopPotionIratorMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -13421569);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, id("time_stop_movement"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, id("time_stop_jump"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.GRAVITY, id("time_stop_gravity"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    protected int attributeAmplifier(int amplifier) {
        return 0;
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        super.createModifiers(0, consumer);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, path);
    }

    @Override
    protected void onStarted(LivingEntity entity, int amplifier) {
        TimeStop.freezeStarted(entity);
        TimeStop.holdStill(entity);
    }

    @Override
    protected void onEnded(LivingEntity entity) {
        TimeStop.freezeEnded(entity);
    }

    @Override
    protected void onTick(LivingEntity entity, int amplifier) {
        TimeStop.holdStill(entity);
    }
}
