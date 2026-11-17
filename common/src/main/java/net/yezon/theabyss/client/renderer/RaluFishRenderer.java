package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelfish3;
import net.yezon.theabyss.entity.RaluFishEntity;

public class RaluFishRenderer extends MobRenderer<RaluFishEntity, Modelfish3<RaluFishEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/ralu_fish.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/ralu_fish.png"));

    public RaluFishRenderer(Context context) {
        super(context, new Modelfish3<>(context.bakeLayer(Modelfish3.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(RaluFishEntity entity) {
        return TEXTURE;
    }
}
