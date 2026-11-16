package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalNeedleEntity;

public class CrystalNeedleRenderer extends EntityRenderer<CrystalNeedleEntity, CrystalNeedleRenderer.State> {
    private static final RenderType SPRITE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/crystal_needle.png"));
    private static final float SIZE = 2.5F;
    private static final int TINT = ARGB.color(255, 255, 110, 220);
    private static final int GLOW = 0xFF3FBE;
    private static final int CORE = 0xFFD2F2;
    private static final float STREAK_TICKS = 1.8F;
    private static final int POINTS = 7;

    public static class State extends EntityRenderState {
        float yaw;
        float pitch;
        float spin;
        float age;
        float stop = -1.0F;
        Vec3 dir = new Vec3(0.0, 0.0, 1.0);
    }

    public CrystalNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(CrystalNeedleEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(CrystalNeedleEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Vec3 v = entity.getDeltaMovement();
        state.yaw = (float)Math.toDegrees(Math.atan2(v.x, v.z));
        state.pitch = (float)Math.toDegrees(Math.atan2(v.y, v.horizontalDistance()));
        state.age = entity.tickCount + partialTick;
        state.spin = entity.roll() + state.age * 40.0F;
        state.stop = entity.stopTick();
        state.dir = v.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : v.normalize();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 eye = camera.pos.subtract(state.x, state.y, state.z);
        boolean stopped = state.stop >= 0.0F;
        float since = stopped ? state.age - state.stop : 0.0F;
        float head = stopped ? state.stop : state.age;
        float tail = Math.max(0.0F, state.age - STREAK_TICKS);
        if (head > tail) {
            Vec3[] points = new Vec3[POINTS];
            float[] glowWidth = new float[POINTS];
            float[] coreWidth = new float[POINTS];
            float[] glow = new float[POINTS];
            float[] core = new float[POINTS];
            for (int i = 0; i < POINTS; i++) {
                float t = head - (head - tail) * i / (POINTS - 1);
                float fresh = Math.max(0.0F, 1.0F - (state.age - t) / STREAK_TICKS);
                points[i] = state.dir.scale(-CrystalNeedleEntity.SPEED * (head - t));
                glowWidth[i] = 0.42F * (0.25F + 0.75F * fresh);
                coreWidth[i] = 0.13F * (0.25F + 0.75F * fresh);
                glow[i] = fresh * fresh * 0.85F;
                core[i] = fresh * fresh;
            }
            collector.submitCustomGeometry(poseStack, LightTrail.STREAK, (pose, buffer) -> {
                LightTrail.ribbon(pose, buffer, points, glowWidth, glow, GLOW, eye);
                LightTrail.ribbon(pose, buffer, points, coreWidth, core, CORE, eye);
            });
        }
        float flash = stopped ? 1.0F - since / CrystalNeedleEntity.FADE : 0.55F;
        float flareSize = stopped ? 0.8F + 1.4F * Math.min(1.0F, since / 2.0F) : 0.9F;
        if (flash > 0.0F) {
            collector.submitCustomGeometry(poseStack, LightTrail.FLARE, (pose, buffer) -> LightTrail.flare(pose, buffer, Vec3.ZERO, flareSize, GLOW, flash, eye));
        }
        if (!stopped) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, state.yaw);
            poseStack.rotateDegrees(Axis.XP, -state.pitch);
            poseStack.rotateDegrees(Axis.ZP, state.spin);
            poseStack.rotateDegrees(Axis.XP, 45.0F);
            collector.submitCustomGeometry(poseStack, SPRITE, (pose, buffer) -> quad(pose, buffer));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer) {
        float h = SIZE * 0.5F;
        int light = 15728880;
        vertex(buffer, pose, -h, -h, 0.0F, 1.0F, light);
        vertex(buffer, pose, h, -h, 1.0F, 1.0F, light);
        vertex(buffer, pose, h, h, 1.0F, 0.0F, light);
        vertex(buffer, pose, -h, h, 0.0F, 0.0F, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float y, float z, float u, float v, int light) {
        buffer.addVertex(pose, 0.0F, y, z).setColor(TINT).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
    }
}
