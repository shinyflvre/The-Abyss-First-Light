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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.NightbladeSpikeEntity;

public class NightbladeSpikeRenderer extends EntityRenderer<NightbladeSpikeEntity, NightbladeSpikeRenderer.State> {
    private static final Identifier BODY = Identifier.fromNamespaceAndPath("theabyss", "textures/entity/nightblade_spike.png");
    private static final RenderType BODY_TYPE = RenderTypes.entityCutout(BODY);
    private static final RenderType GOLEM_BODY_TYPE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("minecraft", "textures/block/amethyst_block.png"));
    private static final RenderType GLOW_TYPE = RenderTypes.eyes(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/nightblade_spike_glow.png"));
    private static final RenderType CRACK_TYPE = RenderTypes.eyes(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/nightblade_crack.png"));
    private static final RenderType ICE_BODY = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("minecraft", "textures/block/packed_ice.png"));
    private static final RenderType ICE_SHELL = RenderTypes.entityTranslucentCull(Identifier.fromNamespaceAndPath("minecraft", "textures/block/ice.png"));
    private static final float[][] ICE_SPIKES = {{0.0F, 0.0F, 2.6F, 0.34F, 0.0F, 0.0F}, {0.38F, 0.12F, 1.4F, 0.22F, 16.0F, 40.0F},
        {-0.3F, -0.26F, 1.1F, 0.2F, 20.0F, 170.0F}, {-0.12F, 0.4F, 0.75F, 0.15F, 26.0F, 285.0F}};
    private static final int CYAN = ARGB.color(255, 90, 215, 255);
    private static final int RED = ARGB.color(255, 255, 94, 94);
    private static final int PINK = ARGB.color(255, 255, 111, 224);
    private static final int GOLEM_BODY = ARGB.color(255, 255, 214, 250);
    private static final float[][] SHARDS = {{0.0F, 0.0F, 2.0F, 0.3F, 0.0F, 0.0F}, {0.32F, 0.1F, 1.15F, 0.2F, 24.0F, 60.0F}, {-0.26F, -0.18F, 0.85F, 0.17F, 30.0F, 200.0F}};

    public static class State extends EntityRenderState {
        float age;
        float yaw;
        int seed;
        boolean red;
        float life = NightbladeSpikeEntity.LIFE;
        boolean frost;
        boolean golem;
    }

    public NightbladeSpikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NightbladeSpikeEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.tickCount + partialTick - entity.warmup();
        state.yaw = entity.getYRot();
        state.seed = entity.getId();
        state.red = entity.isRed();
        state.life = entity.life();
        state.frost = entity.isFrost();
        state.golem = entity.isGolem();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.frost) {
            this.submitIce(state, poseStack, collector);
            super.submit(state, poseStack, collector, camera);
            return;
        }
        int tint = state.golem ? PINK : state.red ? RED : CYAN;
        int body = state.golem ? GOLEM_BODY : -1;
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, -state.yaw);
        float crack = state.age < 0.0F ? Mth.clamp(1.0F + state.age / 12.0F, 0.25F, 1.0F) : Mth.clamp(1.0F - state.age / state.life, 0.0F, 1.0F);
        if (crack > 0.01F) {
            int color = ARGB.color(Math.round(255 * crack), tint);
            collector.submitCustomGeometry(poseStack, CRACK_TYPE, (pose, buffer) -> crack(pose, buffer, color));
        }
        if (state.age >= 0.0F) {
            float rise = rise(state.age, state.life);
            if (rise > 0.0F) {
                float turn = (state.seed * 47) % 360;
                poseStack.rotateDegrees(Axis.YP, turn);
                for (float[] shard : SHARDS) {
                    poseStack.pushPose();
                    poseStack.translate(shard[0], -0.25F, shard[1]);
                    poseStack.rotateDegrees(Axis.YP, shard[5]);
                    poseStack.rotateDegrees(Axis.ZP, shard[4]);
                    float height = shard[2] * rise;
                    float half = shard[3];
                    int light = state.lightCoords;
                    collector.submitCustomGeometry(poseStack, state.golem ? GOLEM_BODY_TYPE : BODY_TYPE, (pose, buffer) -> shard(pose, buffer, height, half, body, light));
                    collector.submitCustomGeometry(poseStack, GLOW_TYPE, (pose, buffer) -> shard(pose, buffer, height, half, tint, 15728880));
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private void submitIce(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.age < 0.0F) {
            return;
        }
        float rise = rise(state.age, state.life);
        if (rise <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, -state.yaw + (state.seed * 47) % 360);
        int light = state.lightCoords;
        int shell = ARGB.color(150, 255, 255, 255);
        for (float[] spike : ICE_SPIKES) {
            poseStack.pushPose();
            poseStack.translate(spike[0], -0.2F, spike[1]);
            poseStack.rotateDegrees(Axis.YP, spike[5]);
            poseStack.rotateDegrees(Axis.ZP, spike[4]);
            float height = spike[2] * rise;
            float half = spike[3];
            collector.submitCustomGeometry(poseStack, ICE_BODY, (pose, buffer) -> icePillar(pose, buffer, height, half, -1, light));
            collector.submitCustomGeometry(poseStack, ICE_SHELL, (pose, buffer) -> icePillar(pose, buffer, height * 1.05F, half * 1.18F, shell, light));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void icePillar(PoseStack.Pose pose, VertexConsumer buffer, float height, float half, int color, int light) {
        int rings = Math.max(2, Mth.ceil(height / 0.5F));
        float[][] dirs = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (int s = 0; s < rings; s++) {
            float f0 = s / (float)rings;
            float f1 = (s + 1) / (float)rings;
            float y0 = height * f0;
            float y1 = height * f1;
            float h0 = half * (float)Math.pow(1.0F - f0, 0.7);
            float h1 = half * (float)Math.pow(1.0F - f1, 0.7);
            float vTop = y0 % 1.0F;
            float vBottom = Math.min(vTop + (y1 - y0), 1.0F);
            float v0 = 1.0F - vBottom;
            float v1 = 1.0F - vTop;
            for (int i = 0; i < 4; i++) {
                float[] a = dirs[i];
                float[] b = dirs[(i + 1) % 4];
                float nx = (a[0] + b[0]) * 0.5F;
                float nz = (a[1] + b[1]) * 0.5F;
                float ny = (h0 - h1) / Math.max(y1 - y0, 1.0E-3F);
                float len = Mth.sqrt(nx * nx + nz * nz + ny * ny);
                nx /= len;
                nz /= len;
                ny /= len;
                float ub0 = 0.5F - h0;
                float ub1 = 0.5F + h0;
                float ut0 = 0.5F - h1;
                float ut1 = 0.5F + h1;
                vertex(buffer, pose, a[0] * h1, y1, a[1] * h1, ut0, v0, color, light, nx, ny, nz);
                vertex(buffer, pose, b[0] * h1, y1, b[1] * h1, ut1, v0, color, light, nx, ny, nz);
                vertex(buffer, pose, b[0] * h0, y0, b[1] * h0, ub1, v1, color, light, nx, ny, nz);
                vertex(buffer, pose, a[0] * h0, y0, a[1] * h0, ub0, v1, color, light, nx, ny, nz);
            }
        }
    }

    private static float rise(float age, float life) {
        if (age < 3.0F) {
            float t = age / 3.0F;
            return 1.15F * (1.0F - (1.0F - t) * (1.0F - t));
        }
        if (age < 5.0F) {
            return Mth.lerp((age - 3.0F) / 2.0F, 1.15F, 1.0F);
        }
        float sinkFrom = life - 7.0F;
        if (age > sinkFrom) {
            return Math.max(0.0F, 1.0F - (age - sinkFrom) / 7.0F);
        }
        return 1.0F;
    }

    private static void shard(PoseStack.Pose pose, VertexConsumer buffer, float height, float half, int color, int light) {
        float[][] corners = {{-half, -half}, {half, -half}, {half, half}, {-half, half}};
        for (int i = 0; i < 4; i++) {
            float[] a = corners[i];
            float[] b = corners[(i + 1) % 4];
            float nx = a[0] + b[0];
            float nz = a[1] + b[1];
            float len = Mth.sqrt(nx * nx + nz * nz + half * half);
            nx /= len;
            nz /= len;
            float ny = half / len;
            vertex(buffer, pose, a[0], 0.0F, a[1], 0.0F, 1.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, b[0], 0.0F, b[1], 1.0F, 1.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, 0.0F, height, 0.0F, 0.5F, 0.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, 0.0F, height, 0.0F, 0.5F, 0.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, 0.0F, height, 0.0F, 0.5F, 0.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, 0.0F, height, 0.0F, 0.5F, 0.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, b[0], 0.0F, b[1], 1.0F, 1.0F, color, light, nx, ny, nz);
            vertex(buffer, pose, a[0], 0.0F, a[1], 0.0F, 1.0F, color, light, nx, ny, nz);
        }
    }

    private static void crack(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        float y = 0.03F;
        float w = 0.35F;
        float l = 0.7F;
        int light = 15728880;
        vertex(buffer, pose, -w, y, -l, 0.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, -w, y, l, 1.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, w, y, l, 1.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, w, y, -l, 0.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, w, y, -l, 0.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, w, y, l, 1.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, -w, y, l, 1.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
        vertex(buffer, pose, -w, y, -l, 0.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color, int light,
                               float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
