package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelfish2;
import net.yezon.theabyss.entity.LuroFishEntity;

public class LuroFishRenderer extends MobRenderer<LuroFishEntity, Modelfish2<LuroFishEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/luro_fish.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/luro_fish.png"));

    public LuroFishRenderer(Context context) {
        super(context, new Modelfish2<>(context.bakeLayer(Modelfish2.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(LuroFishEntity entity) {
        return TEXTURE;
    }
}
