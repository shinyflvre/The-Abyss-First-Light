package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelfrost_spider;
import net.yezon.theabyss.entity.FrostSpiderEntity;

public class FrostSpiderRenderer extends MobRenderer<FrostSpiderEntity, Modelfrost_spider<FrostSpiderEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/frost_spider.png");

    public FrostSpiderRenderer(Context context) {
        super(context, new Modelfrost_spider<>(context.bakeLayer(Modelfrost_spider.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostSpiderEntity entity) {
        return TEXTURE;
    }
}
