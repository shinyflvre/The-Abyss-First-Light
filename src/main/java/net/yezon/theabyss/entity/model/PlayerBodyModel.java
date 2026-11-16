package net.yezon.theabyss.entity.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.PlayerBodyEntity;

public class PlayerBodyModel extends GeoModel<PlayerBodyEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "player_body");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/player_body.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(PlayerBodyEntity animatable) {
        return MODEL;
    }
}
