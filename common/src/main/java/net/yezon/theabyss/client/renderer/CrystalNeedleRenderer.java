package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalNeedleEntity;

public class CrystalNeedleRenderer extends EntityRenderer<CrystalNeedleEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("theabyss", "textures/entity/crystal_needle.png");
    private static final RenderType SPRITE = RenderType.entityCutoutNoCull(TEXTURE);
    private static final float SIZE = 2.5F;
    private static final int TINT = ARGB.color(255, 255, 110, 220);
    private static final int GLOW = 0xFF3FBE;
    private static final int CORE = 0xFFD2F2;
    private static final float STREAK_TICKS = 1.8F;
    private static final int POINTS = 7;

    public CrystalNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(CrystalNeedleEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(CrystalNeedleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Vec3 v = entity.getDeltaMovement();
        float yaw = (float)Math.toDegrees(Math.atan2(v.x, v.z));
        float pitch = (float)Math.toDegrees(Math.atan2(v.y, v.horizontalDistance()));
        float age = entity.tickCount + partialTick;
        float spin = entity.roll() + age * 40.0F;
        float stop = entity.stopTick();
        Vec3 dir = v.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : v.normalize();
        Vec3 eye = this.entityRenderDispatcher.camera.getPosition().subtract(RenderTicks.position(entity, partialTick));
        boolean stopped = stop >= 0.0F;
        float since = stopped ? age - stop : 0.0F;
        float head = stopped ? stop : age;
        float tail = Math.max(0.0F, age - STREAK_TICKS);
        if (head > tail) {
            Vec3[] points = new Vec3[POINTS];
            float[] glowWidth = new float[POINTS];
            float[] coreWidth = new float[POINTS];
            float[] glow = new float[POINTS];
            float[] core = new float[POINTS];
            for (int i = 0; i < POINTS; i++) {
                float t = head - (head - tail) * i / (POINTS - 1);
                float fresh = Math.max(0.0F, 1.0F - (age - t) / STREAK_TICKS);
                points[i] = dir.scale(-CrystalNeedleEntity.SPEED * (head - t));
                glowWidth[i] = 0.42F * (0.25F + 0.75F * fresh);
                coreWidth[i] = 0.13F * (0.25F + 0.75F * fresh);
                glow[i] = fresh * fresh * 0.85F;
                core[i] = fresh * fresh;
            }
            VertexConsumer streak = buffers.getBuffer(LightTrail.STREAK);
            LightTrail.ribbon(poseStack.last(), streak, points, glowWidth, glow, GLOW, eye);
            LightTrail.ribbon(poseStack.last(), streak, points, coreWidth, core, CORE, eye);
        }
        float flash = stopped ? 1.0F - since / CrystalNeedleEntity.FADE : 0.55F;
        float flareSize = stopped ? 0.8F + 1.4F * Math.min(1.0F, since / 2.0F) : 0.9F;
        if (flash > 0.0F) {
            LightTrail.flare(poseStack.last(), buffers.getBuffer(LightTrail.FLARE), Vec3.ZERO, flareSize, GLOW, flash, eye);
        }
        if (!stopped) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
            quad(poseStack.last(), buffers.getBuffer(SPRITE));
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CrystalNeedleEntity entity) {
        return TEXTURE;
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
