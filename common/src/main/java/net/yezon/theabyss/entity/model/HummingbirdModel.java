package net.yezon.theabyss.entity.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.HummingbirdEntity;
import org.jspecify.annotations.Nullable;

public class HummingbirdModel extends GeoModel<HummingbirdEntity> {
    public static final DataTicket<String> TEXTURE = DataTicket.create("theabyss_hummingbird_texture", String.class);
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "hummingbird");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "hummingbird");

    @Override
    public Identifier getAnimationResource(HummingbirdEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("theabyss", "textures/entity/" + renderState.getOrDefaultGeckolibData(TEXTURE, "hummingbird") + ".png");
    }

    @Override
    public void addAdditionalStateData(HummingbirdEntity entity, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(TEXTURE, entity.getTexture());
    }
}
