package net.yezon.theabyss.entity.layer;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.HummingbirdEntity;
import software.bernie.geckolib.renderer.GeoRenderer;

public class HummingbirdLayer extends GlowGeoLayer<HummingbirdEntity> {
    public static final ResourceLocation INFECTED = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/infected_hummingbird_glow.png");
    public static final ResourceLocation VIGILANT = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/vigilant_hummingbird_glow.png");

    public HummingbirdLayer(GeoRenderer<HummingbirdEntity> entityRenderer, ResourceLocation texture) {
        super(entityRenderer, texture);
    }
}
