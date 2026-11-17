package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelfish1;
import net.yezon.theabyss.entity.LaroFishEntity;

public class LaroFishRenderer extends MobRenderer<LaroFishEntity, Modelfish1<LaroFishEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/laro_fish.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/laro_fish.png"));

    public LaroFishRenderer(Context context) {
        super(context, new Modelfish1<>(context.bakeLayer(Modelfish1.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(LaroFishEntity entity) {
        return TEXTURE;
    }
}
