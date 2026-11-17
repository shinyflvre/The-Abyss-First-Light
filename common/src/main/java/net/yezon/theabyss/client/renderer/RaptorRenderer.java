package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelabyss_raptor;
import net.yezon.theabyss.entity.RaptorEntity;

public class RaptorRenderer extends MobRenderer<RaptorEntity, Modelabyss_raptor<RaptorEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/raptor.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/raptor_glow.png"));

    public RaptorRenderer(Context context) {
        super(context, new Modelabyss_raptor<>(context.bakeLayer(Modelabyss_raptor.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(RaptorEntity entity) {
        return TEXTURE;
    }
}
