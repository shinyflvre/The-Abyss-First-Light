package net.yezon.theabyss.entity.layer;

import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import software.bernie.geckolib.renderer.GeoRenderer;

public class InfectedDragonflyLayer extends GlowGeoLayer<InfectedDragonflyEntity> {
    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/infected_dragonfly_glow.png");

    public InfectedDragonflyLayer(GeoRenderer<InfectedDragonflyEntity> entityRenderer) {
        super(entityRenderer, LAYER);
    }
}
