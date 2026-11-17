package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.EndSpiderEntity;

public class EndSpiderRenderer extends MobRenderer<EndSpiderEntity, SpiderModel<EndSpiderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/end_spider.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/end_spider_glow.png"));

    public EndSpiderRenderer(Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(EndSpiderEntity entity) {
        return TEXTURE;
    }
}
