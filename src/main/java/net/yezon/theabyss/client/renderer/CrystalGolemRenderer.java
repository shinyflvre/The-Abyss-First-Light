package net.yezon.theabyss.client.renderer;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.CrystalGolemEntity;
import net.yezon.theabyss.entity.model.CrystalGolemModel;
import org.jspecify.annotations.Nullable;

public class CrystalGolemRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<CrystalGolemEntity, R> {
    private static final Identifier GLOW = Identifier.parse("theabyss:textures/entity/crystal_golem_glow.png");
    static final DataTicket<Float> FIST_GLOW = DataTicket.create("theabyss_golem_fist_glow", Float.class);

    public CrystalGolemRenderer(Context context) {
        super(context, new CrystalGolemModel());
        this.shadowRadius = 1.1F;
        this.withRenderLayer(new TextureLayerGeoLayer<>(this, GLOW, RenderTypes::eyes));
        this.withRenderLayer(new FistGlowLayer<>(this));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        float pitch = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0.0F);
        float yaw = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0.0F);
        snapshots.ifPresent("mob_head", bone -> {
            bone.setRotX(bone.getRotX() - pitch * Mth.DEG_TO_RAD);
            bone.setRotY(bone.getRotY() - yaw * Mth.DEG_TO_RAD);
        });
    }

    static final class FistGlowLayer<R extends LivingEntityRenderState & GeoRenderState> extends CustomBoneTextureGeoLayer<CrystalGolemEntity, Void, R> {
        FistGlowLayer(GeoRenderer<CrystalGolemEntity, Void, R> renderer) {
            super(renderer, "left_arm", CrystalGolemModel.TEXTURE);
        }

        @Override
        public void addRenderData(CrystalGolemEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
            renderState.addGeckolibData(FIST_GLOW, animatable.fistGlow(partialTick));
        }

        @Override
        public boolean shouldRenderBone(R renderState) {
            return renderState.getOrDefaultGeckolibData(FIST_GLOW, 0.0F) > 0.01F;
        }

        @Override
        public void preRender(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        }

        @Override
        protected @Nullable RenderType getRenderType(R renderState, Identifier texture) {
            return RenderTypes.eyes(texture);
        }

        @Override
        protected void renderBone(RenderPassInfo<R> renderPassInfo, GeoBone bone, SubmitNodeCollector renderTasks) {
            float glow = renderPassInfo.renderState().getOrDefaultGeckolibData(FIST_GLOW, 0.0F);
            int alpha = (int)(Mth.clamp(glow, 0.0F, 1.0F) * 215.0F);
            int color = ARGB.color(alpha, 255, 120, 235);
            int light = renderPassInfo.packedLight();
            int overlay = renderPassInfo.packedOverlay();
            renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), RenderTypes.eyes(CrystalGolemModel.TEXTURE), (pose, buffer) -> {
                PoseStack poseStack = renderPassInfo.poseStack();
                poseStack.pushPose();
                poseStack.last().set(pose);
                bone.translateAwayFromPivotPoint(poseStack);
                for (GeoCube cube : ((CuboidGeoBone)bone).cubes) {
                    poseStack.pushPose();
                    this.renderCube(cube, poseStack, buffer, light, overlay, color, 1.0F, 1.0F);
                    poseStack.popPose();
                }
                poseStack.popPose();
            });
        }
    }
}
