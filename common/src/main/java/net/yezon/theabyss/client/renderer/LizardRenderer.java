package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.LizardEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.LizardModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LizardRenderer extends GeoEntityRenderer<LizardEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/lizard.png");

    public LizardRenderer(Context context) {
        super(context, new LizardModel());
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new GlowGeoLayer<>(this, TEXTURE));
    }

    @Override
    protected void applyRotations(LizardEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        float blend = Mth.lerp(partialTick, animatable.wallBlendO, animatable.wallBlend);
        if (blend > 0.001F) {
            float s = blend * blend * (3.0F - 2.0F * blend);
            poseStack.translate(0.0F, 0.45F * s, -0.405F * s);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * s));
        }
    }
}
