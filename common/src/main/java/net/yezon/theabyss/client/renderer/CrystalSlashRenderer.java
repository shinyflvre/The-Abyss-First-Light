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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalSlashEntity;

public class CrystalSlashRenderer extends EntityRenderer<CrystalSlashEntity, CrystalSlashRenderer.State> {
    private static final RenderType[] FRAMES = new RenderType[8];
    private static final RenderType[] GLOW_FRAMES = new RenderType[8];
    private static final int TINT = 0xFF82E1;
    private static final int GLOW = 0xFF3FBE;
    private static final int CORE = 0xFFD2F2;
    private static final float TRAIL_TICKS = 5.0F;
    private static final int ROWS = 12;

    static {
        for (int i = 0; i < 8; i++) {
            Identifier sweep = Identifier.withDefaultNamespace("textures/particle/sweep_" + i + ".png");
            FRAMES[i] = RenderTypes.entityTranslucent(sweep);
            GLOW_FRAMES[i] = LightTrail.type(sweep);
        }
    }

    public static class State extends EntityRenderState {
        float yaw;
        float pitch;
        float age;
        float half;
        float sway;
        float stop = -1.0F;
        Vec3 dir = new Vec3(0.0, 0.0, 1.0);
    }

    public CrystalSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(CrystalSlashEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(CrystalSlashEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        Vec3 v = entity.getDeltaMovement();
        state.yaw = (float)Math.toDegrees(Math.atan2(v.x, v.z));
        state.pitch = (float)Math.toDegrees(Math.atan2(v.y, v.horizontalDistance()));
        state.age = entity.tickCount + partialTick;
        state.stop = entity.stopTick();
        state.half = (float)CrystalSlashEntity.halfWidth(state.stop >= 0 ? state.stop : state.age);
        state.sway = (entity.getId() % 30 - 15) * Mth.sin(state.age * 0.045F);
        state.dir = v.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : v.normalize();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean stopped = state.stop >= 0.0F;
        float life = stopped ? 1.0F - (state.age - state.stop) / CrystalSlashEntity.FADE
            : Mth.clamp((CrystalSlashEntity.MAX_AGE + 1 - state.age) / CrystalSlashEntity.FADE, 0.0F, 1.0F);
        if (life <= 0.0F) {
            super.submit(state, poseStack, collector, camera);
            return;
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yaw);
        poseStack.rotateDegrees(Axis.XP, -state.pitch);
        poseStack.rotateDegrees(Axis.ZP, state.sway);
        this.trail(state, poseStack, collector, life);
        int frame = (int)(state.age * 0.5F);
        int alpha = Math.round(255 * life);
        poseStack.rotateDegrees(Axis.YP, -15.0F);
        poseStack.rotateDegrees(Axis.ZP, -10.0F);
        this.crescent(poseStack, collector, (frame + 4) % 8, state.half, alpha, life);
        poseStack.rotateDegrees(Axis.YP, 30.0F);
        poseStack.rotateDegrees(Axis.ZP, 20.0F);
        this.crescent(poseStack, collector, frame % 8, state.half, alpha, life);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private void crescent(PoseStack poseStack, SubmitNodeCollector collector, int frame, float half, int alpha, float life) {
        collector.submitCustomGeometry(poseStack, FRAMES[frame], (pose, buffer) -> quad(pose, buffer, half, ARGB.color(alpha, TINT)));
        int light = scale(GLOW, 0.7F * life);
        collector.submitCustomGeometry(poseStack, GLOW_FRAMES[frame], (pose, buffer) -> quad(pose, buffer, half * 1.04F, ARGB.color(255, light)));
    }

    private void trail(State state, PoseStack poseStack, SubmitNodeCollector collector, float life) {
        float head = state.stop >= 0.0F ? state.stop : state.age;
        float tail = Math.max(0.0F, state.age - TRAIL_TICKS);
        if (head <= tail) {
            return;
        }
        Vec3[] left = new Vec3[ROWS];
        Vec3[] right = new Vec3[ROWS];
        float[] light = new float[ROWS];
        int[] rgb = new int[ROWS];
        for (int i = 0; i < ROWS; i++) {
            float t = head - (head - tail) * i / (ROWS - 1);
            float fresh = Math.max(0.0F, 1.0F - (state.age - t) / TRAIL_TICKS);
            float z = (float)(-CrystalSlashEntity.SPEED * (head - t));
            float half = (float)CrystalSlashEntity.halfWidth(t) * 0.95F;
            left[i] = new Vec3(-half, -0.1, z);
            right[i] = new Vec3(half, -0.1, z);
            light[i] = (float)Math.pow(fresh, 1.2) * life;
            rgb[i] = ARGB.srgbLerp(fresh * fresh, GLOW, CORE) & 0xFFFFFF;
        }
        collector.submitCustomGeometry(poseStack, LightTrail.SHEET, (pose, buffer) -> LightTrail.band(pose, buffer, left, right, light, rgb));
    }

    private static int scale(int rgb, float k) {
        return Math.round((rgb >> 16 & 0xFF) * k) << 16 | Math.round((rgb >> 8 & 0xFF) * k) << 8 | Math.round((rgb & 0xFF) * k);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float half, int color) {
        vertex(buffer, pose, -half, -half, 0.0F, 1.0F, color);
        vertex(buffer, pose, half, -half, 1.0F, 1.0F, color);
        vertex(buffer, pose, half, half, 1.0F, 0.0F, color);
        vertex(buffer, pose, -half, half, 0.0F, 0.0F, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, -0.1F, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
