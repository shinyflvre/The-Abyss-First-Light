package net.yezon.theabyss.entity.model;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import org.jspecify.annotations.Nullable;

public class InfectedDragonflyModel extends GeoModel<InfectedDragonflyEntity> {
    public static final DataTicket<String> TEXTURE = DataTicket.create("theabyss_infected_dragonfly_texture", String.class);
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("theabyss", "dragonfly");
    private static final Identifier ANIMATIONS = Identifier.fromNamespaceAndPath("theabyss", "dragonfly");

    @Override
    public Identifier getAnimationResource(InfectedDragonflyEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("theabyss", "textures/entity/" + renderState.getOrDefaultGeckolibData(TEXTURE, "dragonfly") + ".png");
    }

    @Override
    public void addAdditionalStateData(InfectedDragonflyEntity entity, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(TEXTURE, entity.getTexture());
    }
}
