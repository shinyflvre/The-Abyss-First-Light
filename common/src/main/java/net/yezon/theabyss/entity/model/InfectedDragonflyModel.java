package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import software.bernie.geckolib.model.GeoModel;

public class InfectedDragonflyModel extends GeoModel<InfectedDragonflyEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/dragonfly.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/dragonfly.animation.json");

    @Override
    public ResourceLocation getModelResource(InfectedDragonflyEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(InfectedDragonflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/" + animatable.getTexture() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfectedDragonflyEntity animatable) {
        return ANIMATIONS;
    }
}
