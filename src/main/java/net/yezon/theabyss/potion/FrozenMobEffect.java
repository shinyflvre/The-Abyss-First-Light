package net.yezon.theabyss.potion;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yezon.theabyss.TheabyssMod;

public class FrozenMobEffect extends MobEffect {
    public FrozenMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xB5F7FF);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "effect.frozen.speed"), -0.92,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.FLYING_SPEED, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "effect.frozen.flying"), -0.92,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "effect.frozen.jump"), -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
