package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.CrystalGolemEntity;

public class CrystalGolemModel extends GeoModel<CrystalGolemEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "crystal_golem");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "crystal_golem");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/crystal_golem.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(CrystalGolemEntity entity) {
        return ANIMATIONS;
    }
}
