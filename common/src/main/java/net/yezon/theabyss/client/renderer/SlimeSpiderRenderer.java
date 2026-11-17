package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelslime_spider;
import net.yezon.theabyss.entity.SlimeSpiderEntity;

public class SlimeSpiderRenderer extends MobRenderer<SlimeSpiderEntity, Modelslime_spider<SlimeSpiderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/slime_spider.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/slime_spider_glow.png"));

    public SlimeSpiderRenderer(Context context) {
        super(context, new Modelslime_spider<>(context.bakeLayer(Modelslime_spider.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeSpiderEntity entity) {
        return TEXTURE;
    }
}
