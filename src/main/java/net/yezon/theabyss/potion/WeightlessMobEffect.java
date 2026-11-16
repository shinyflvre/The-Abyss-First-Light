package net.yezon.theabyss.potion;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yezon.theabyss.TheabyssMod;

public class WeightlessMobEffect extends MobEffect {
    public WeightlessMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x5CC8FF);
        this.addAttributeModifier(Attributes.GRAVITY, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "weightless"),
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, amplifier -> -1.0);
    }
}
