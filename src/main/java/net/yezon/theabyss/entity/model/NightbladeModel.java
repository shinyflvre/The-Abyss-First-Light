package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.NightbladeBossEntity;

public class NightbladeModel extends GeoModel<NightbladeBossEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "nightblade");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "nightblade");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/nightblade_boss.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(NightbladeBossEntity entity) {
        return ANIMATIONS;
    }
}
