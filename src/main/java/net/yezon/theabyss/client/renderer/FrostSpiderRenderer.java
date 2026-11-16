package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelfrost_spider;
import net.yezon.theabyss.entity.FrostSpiderEntity;

public class FrostSpiderRenderer extends MobRenderer<FrostSpiderEntity, LivingEntityRenderState, Modelfrost_spider<LivingEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/frost_spider.png");

    public FrostSpiderRenderer(Context context) {
        super(context, new Modelfrost_spider<>(context.bakeLayer(Modelfrost_spider.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
