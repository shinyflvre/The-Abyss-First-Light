package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.RiftNightbladeEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.RiftNightbladeModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RiftNightbladeRenderer extends GeoEntityRenderer<RiftNightbladeEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/nightblade_glowing_layer.png");

    public RiftNightbladeRenderer(Context context) {
        super(context, new RiftNightbladeModel());
        this.shadowRadius = 0.7F;
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW));
    }
}
