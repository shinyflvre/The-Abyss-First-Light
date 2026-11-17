package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelglow_pug;
import net.yezon.theabyss.entity.GlowPugEntity;

public class GlowPugRenderer extends MobRenderer<GlowPugEntity, Modelglow_pug<GlowPugEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/glow_pug.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/glow_pug_glow.png"));

    public GlowPugRenderer(Context context) {
        super(context, new Modelglow_pug<>(context.bakeLayer(Modelglow_pug.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(GlowPugEntity entity) {
        return TEXTURE;
    }
}
