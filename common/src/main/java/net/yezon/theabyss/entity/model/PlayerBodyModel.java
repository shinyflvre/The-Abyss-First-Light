package net.yezon.theabyss.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.PlayerBodyEntity;
import software.bernie.geckolib.model.GeoModel;

public class PlayerBodyModel extends GeoModel<PlayerBodyEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("theabyss", "geo/player_body.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("theabyss", "animations/player_body.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/player_body.png");

    @Override
    public ResourceLocation getModelResource(PlayerBodyEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PlayerBodyEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerBodyEntity animatable) {
        return ANIMATIONS;
    }
}
