package net.yezon.theabyss.entity.layer;

import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.HummingbirdEntity;

public class HummingbirdLayer<R extends GeoRenderState> extends TextureLayerGeoLayer<HummingbirdEntity, Void, R> {
    public static final Identifier INFECTED = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/infected_hummingbird_glow.png");
    public static final Identifier VIGILANT = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/vigilant_hummingbird_glow.png");

    public HummingbirdLayer(GeoRenderer<HummingbirdEntity, Void, R> entityRenderer, Identifier texture) {
        super(entityRenderer, texture, RenderTypes::eyes);
    }
}
