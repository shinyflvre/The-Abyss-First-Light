package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelmagic;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;

public class RingOfFireStrikeAttackProjectileRenderer
    extends EntityRenderer<RingOfFireStrikeAttackProjectileEntity, RingOfFireStrikeAttackProjectileRenderer.State> {
    private static final Identifier texture = Identifier.parse("theabyss:textures/entity/fire_strike.png");
    private final Modelmagic<EntityRenderState> model;

    public RingOfFireStrikeAttackProjectileRenderer(Context context) {
        super(context);
        this.model = new Modelmagic<>(context.bakeLayer(Modelmagic.LAYER_LOCATION));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        poseStack.rotateDegrees(Axis.ZP, 90.0F + state.xRot);
        submitNodeCollector.submitModelPart(
            this.model.root(), poseStack, RenderTypes.entityCutoutCull(this.getTextureLocation(state)), state.lightCoords, OverlayTexture.NO_OVERLAY, null
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RingOfFireStrikeAttackProjectileEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
    }

    public Identifier getTextureLocation(State state) {
        return texture;
    }

    public static class State extends EntityRenderState {
        public float xRot;
        public float yRot;
    }
}
