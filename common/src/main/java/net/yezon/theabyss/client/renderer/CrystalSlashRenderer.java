package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalSlashEntity;

public class CrystalSlashRenderer extends EntityRenderer<CrystalSlashEntity> {
    private static final RenderType[] FRAMES = new RenderType[8];
    private static final RenderType[] GLOW_FRAMES = new RenderType[8];
    private static final int TINT = 0xFF82E1;
    private static final int GLOW = 0xFF3FBE;
    private static final int CORE = 0xFFD2F2;
    private static final float TRAIL_TICKS = 5.0F;
    private static final int ROWS = 12;

    static {
        for (int i = 0; i < 8; i++) {
            ResourceLocation sweep = ResourceLocation.withDefaultNamespace("textures/particle/sweep_" + i + ".png");
            FRAMES[i] = RenderType.entityTranslucent(sweep);
            GLOW_FRAMES[i] = LightTrail.type(sweep);
        }
    }

    public CrystalSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(CrystalSlashEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(CrystalSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Vec3 v = entity.getDeltaMovement();
        float yaw = (float)Math.toDegrees(Math.atan2(v.x, v.z));
        float pitch = (float)Math.toDegrees(Math.atan2(v.y, v.horizontalDistance()));
        float age = entity.tickCount + partialTick;
        float stop = entity.stopTick();
        float half = (float)CrystalSlashEntity.halfWidth(stop >= 0 ? stop : age);
        float sway = (entity.getId() % 30 - 15) * Mth.sin(age * 0.045F);
        boolean stopped = stop >= 0.0F;
        float life = stopped ? 1.0F - (age - stop) / CrystalSlashEntity.FADE
            : Mth.clamp((CrystalSlashEntity.MAX_AGE + 1 - age) / CrystalSlashEntity.FADE, 0.0F, 1.0F);
        if (life <= 0.0F) {
            super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
        this.trail(age, stop, poseStack, buffers, life);
        int frame = (int)(age * 0.5F);
        int alpha = Math.round(255 * life);
        poseStack.mulPose(Axis.YP.rotationDegrees(-15.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F));
        this.crescent(poseStack, buffers, (frame + 4) % 8, half, alpha, life);
        poseStack.mulPose(Axis.YP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
        this.crescent(poseStack, buffers, frame % 8, half, alpha, life);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CrystalSlashEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private void crescent(PoseStack poseStack, MultiBufferSource buffers, int frame, float half, int alpha, float life) {
        quad(poseStack.last(), buffers.getBuffer(FRAMES[frame]), half, ARGB.color(alpha, TINT));
        int light = scale(GLOW, 0.7F * life);
        quad(poseStack.last(), buffers.getBuffer(GLOW_FRAMES[frame]), half * 1.04F, ARGB.color(255, light));
    }

    private void trail(float age, float stop, PoseStack poseStack, MultiBufferSource buffers, float life) {
        float head = stop >= 0.0F ? stop : age;
        float tail = Math.max(0.0F, age - TRAIL_TICKS);
        if (head <= tail) {
            return;
        }
        Vec3[] left = new Vec3[ROWS];
        Vec3[] right = new Vec3[ROWS];
        float[] light = new float[ROWS];
        int[] rgb = new int[ROWS];
        for (int i = 0; i < ROWS; i++) {
            float t = head - (head - tail) * i / (ROWS - 1);
            float fresh = Math.max(0.0F, 1.0F - (age - t) / TRAIL_TICKS);
            float z = (float)(-CrystalSlashEntity.SPEED * (head - t));
            float half = (float)CrystalSlashEntity.halfWidth(t) * 0.95F;
            left[i] = new Vec3(-half, -0.1, z);
            right[i] = new Vec3(half, -0.1, z);
            light[i] = (float)Math.pow(fresh, 1.2) * life;
            rgb[i] = ARGB.srgbLerp(fresh * fresh, GLOW, CORE) & 0xFFFFFF;
        }
        LightTrail.band(poseStack.last(), buffers.getBuffer(LightTrail.SHEET), left, right, light, rgb);
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
