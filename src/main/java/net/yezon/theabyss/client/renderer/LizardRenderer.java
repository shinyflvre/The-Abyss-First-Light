package net.yezon.theabyss.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.LizardEntity;
import net.yezon.theabyss.entity.model.LizardModel;

public class LizardRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<LizardEntity, R> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/lizard.png");

    public LizardRenderer(Context context) {
        super(context, new LizardModel());
        this.shadowRadius = 0.5F;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, TEXTURE, RenderTypes::eyes));
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);
        float blend = renderPassInfo.getOrDefaultGeckolibData(LizardModel.WALL_BLEND, 0.0F);
        if (blend > 0.001F) {
            float s = blend * blend * (3.0F - 2.0F * blend);
            poseStack.translate(0.0F, 0.45F * s, -0.405F * s);
            poseStack.rotateDegrees(Axis.XP, 90.0F * s);
        }
    }
}
