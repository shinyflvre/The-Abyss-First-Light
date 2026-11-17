package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.TheRokaEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.TheRokaModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TheRokaRenderer extends GeoEntityRenderer<TheRokaEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/the_roka_glow.png");

    public TheRokaRenderer(Context context) {
        super(context, new TheRokaModel());
        this.withScale(0.525F);
        this.shadowRadius = 0.8F;
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW));
    }
}
