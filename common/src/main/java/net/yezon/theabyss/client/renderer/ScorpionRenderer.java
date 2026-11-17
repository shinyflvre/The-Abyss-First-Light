package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelscorpion;
import net.yezon.theabyss.entity.ScorpionEntity;

public class ScorpionRenderer extends MobRenderer<ScorpionEntity, Modelscorpion<ScorpionEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/scorpion.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/scorpion_glow.png"));

    public ScorpionRenderer(Context context) {
        super(context, new Modelscorpion<>(context.bakeLayer(Modelscorpion.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(ScorpionEntity entity) {
        return TEXTURE;
    }
}
