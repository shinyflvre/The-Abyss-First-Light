package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.LizardEntity;
import software.bernie.geckolib.model.GeoModel;

public class LizardModel extends GeoModel<LizardEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/lizard.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/lizard.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/lizard.png");

    @Override
    public ResourceLocation getModelResource(LizardEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LizardEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LizardEntity animatable) {
        return ANIMATIONS;
    }
}
