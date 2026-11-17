package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelmagic;
import net.yezon.theabyss.entity.MagicEntity;

public class MagicRenderer extends MobRenderer<MagicEntity, Modelmagic<MagicEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/magic.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/magic.png"));

    public MagicRenderer(Context context) {
        super(context, new Modelmagic<>(context.bakeLayer(Modelmagic.LAYER_LOCATION)), 0.6F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    protected void scale(MagicEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(3.0F, 3.0F, 3.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicEntity entity) {
        return TEXTURE;
    }
}
