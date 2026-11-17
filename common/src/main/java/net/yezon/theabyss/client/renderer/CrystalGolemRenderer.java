package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.CrystalGolemEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.CrystalGolemModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CrystalGolemRenderer extends GeoEntityRenderer<CrystalGolemEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/crystal_golem_glow.png");

    public CrystalGolemRenderer(Context context) {
        super(context, new CrystalGolemModel());
        this.shadowRadius = 1.1F;
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW));
        this.addRenderLayer(new FistGlowLayer(this));
    }

    static final class FistGlowLayer extends GeoRenderLayer<CrystalGolemEntity> {
        private static final String BONE = "left_arm";

        FistGlowLayer(GeoRenderer<CrystalGolemEntity> renderer) {
            super(renderer);
        }

        @Override
        public void renderForBone(PoseStack poseStack, CrystalGolemEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (renderType == null || !BONE.equals(bone.getName())) {
                return;
            }
            float glow = animatable.fistGlow(partialTick);
            if (glow <= 0.01F) {
                return;
            }
            int alpha = (int)(Mth.clamp(glow, 0.0F, 1.0F) * 215.0F);
            int color = ARGB.color(alpha, 255, 120, 235);
            VertexConsumer glowBuffer = bufferSource.getBuffer(AbyssRenderTypes.eyes(CrystalGolemModel.TEXTURE));
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                this.getRenderer().renderCube(poseStack, cube, glowBuffer, packedLight, packedOverlay, color);
                poseStack.popPose();
            }
        }
    }
}
