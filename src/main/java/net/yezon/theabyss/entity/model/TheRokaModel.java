package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.TheRokaEntity;

public class TheRokaModel extends GeoModel<TheRokaEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "the_roka");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "the_roka");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/the_roka.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(TheRokaEntity entity) {
        return ANIMATIONS;
    }
}
