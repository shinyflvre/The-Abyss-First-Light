package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.AbyssaurEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.AbyssaurModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbyssaurRenderer extends GeoEntityRenderer<AbyssaurEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/abyssaur_glow.png");

    public AbyssaurRenderer(Context context) {
        super(context, new AbyssaurModel());
        this.shadowRadius = 1.2F;
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW));
    }
}
