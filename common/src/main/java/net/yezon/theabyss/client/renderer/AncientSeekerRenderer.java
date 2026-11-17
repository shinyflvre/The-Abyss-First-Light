package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelancient_seeker;
import net.yezon.theabyss.entity.AncientSeekerEntity;

public class AncientSeekerRenderer extends MobRenderer<AncientSeekerEntity, Modelancient_seeker<AncientSeekerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/ancient_seeker.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/ancient_seeker_glow.png"));

    public AncientSeekerRenderer(Context context) {
        super(context, new Modelancient_seeker<>(context.bakeLayer(Modelancient_seeker.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    protected void scale(AncientSeekerEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AncientSeekerEntity entity) {
        return TEXTURE;
    }
}
