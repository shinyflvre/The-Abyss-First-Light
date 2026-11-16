package net.yezon.theabyss.entity.layer;

import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;

public class InfectedDragonflyLayer<R extends GeoRenderState> extends TextureLayerGeoLayer<InfectedDragonflyEntity, Void, R> {
    private static final Identifier LAYER = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/infected_dragonfly_glow.png");

    public InfectedDragonflyLayer(GeoRenderer<InfectedDragonflyEntity, Void, R> entityRenderer) {
        super(entityRenderer, LAYER, RenderTypes::eyes);
    }
}
