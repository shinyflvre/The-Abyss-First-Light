package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelskeleton_abyss;
import net.yezon.theabyss.entity.HauntedSkeletonEntity;

public class HauntedSkeletonRenderer extends MobRenderer<HauntedSkeletonEntity, Modelskeleton_abyss<HauntedSkeletonEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/haunted_skeleton.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/haunted_skeleton_glow.png"));

    public HauntedSkeletonRenderer(Context context) {
        super(context, new Modelskeleton_abyss<>(context.bakeLayer(Modelskeleton_abyss.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(HauntedSkeletonEntity entity) {
        return TEXTURE;
    }
}
