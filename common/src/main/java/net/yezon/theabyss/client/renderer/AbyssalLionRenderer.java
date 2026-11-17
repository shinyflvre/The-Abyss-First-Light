package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelabyssal_lion;
import net.yezon.theabyss.entity.AbyssalLionEntity;

public class AbyssalLionRenderer extends MobRenderer<AbyssalLionEntity, Modelabyssal_lion<AbyssalLionEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/abyssal_lion.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/abyssal_lion_glow.png"));

    public AbyssalLionRenderer(Context context) {
        super(context, new Modelabyssal_lion<>(context.bakeLayer(Modelabyssal_lion.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    protected void scale(AbyssalLionEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalLionEntity entity) {
        return TEXTURE;
    }
}
