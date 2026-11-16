package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.AbyssaurEntity;

public class AbyssaurModel extends GeoModel<AbyssaurEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "abyssaur");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "abyssaur");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/abyssaur.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(AbyssaurEntity entity) {
        return ANIMATIONS;
    }
}
