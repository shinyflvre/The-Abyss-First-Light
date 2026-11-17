package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.HummingbirdEntity;
import software.bernie.geckolib.model.GeoModel;

public class HummingbirdModel extends GeoModel<HummingbirdEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/hummingbird.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/hummingbird.animation.json");

    @Override
    public ResourceLocation getModelResource(HummingbirdEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HummingbirdEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/" + animatable.getTexture() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(HummingbirdEntity animatable) {
        return ANIMATIONS;
    }
}
