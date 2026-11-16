package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.JungleLurkerEntity;

public class LurkerModel extends GeoModel<JungleLurkerEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "lurker");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "lurker");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/lurker.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(JungleLurkerEntity entity) {
        return ANIMATIONS;
    }
}
