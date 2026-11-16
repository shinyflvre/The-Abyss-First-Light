package net.yezon.theabyss.potion;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yezon.theabyss.TheabyssMod;

public class WeightlessMobEffect extends MobEffect {
    public WeightlessMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x5CC8FF);
        this.addAttributeModifier(Attributes.GRAVITY, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "weightless"), -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        super.createModifiers(0, consumer);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, 0);
    }
}
