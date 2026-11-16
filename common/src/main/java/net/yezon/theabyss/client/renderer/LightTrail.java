package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class LightTrail {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(net.yezon.theabyss.mixin.accessor.client.RenderPipelinesAccessor.theabyss$energySwirlSnippet())
        .withLocation(Identifier.fromNamespaceAndPath("theabyss", "pipeline/light_trail"))
        .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
        .build();
    public static final RenderType STREAK = type(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/light_trail.png"));
    public static final RenderType SHEET = type(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/light_sheet.png"));
    public static final RenderType FLARE =type(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/light_flare.png"));
    private static final int FULL_BRIGHT = 15728880;

    private LightTrail() {
    }

    public static RenderType type(Identifier texture) {
        return net.yezon.theabyss.mixin.accessor.client.RenderTypeAccessor.theabyss$create("theabyss_light_trail", RenderSetup.builder(PIPELINE)
            .withTexture("Sampler0", texture)
            .setTextureTransform(TextureTransform.DEFAULT_TEXTURING)
            .useLightmap()
            .useOverlay()
            .sortOnUpload()
            .createRenderSetup());
    }

    public static void ribbon(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, float[] width, float[] light, int rgb, Vec3 camera) {
        int n = points.length;
        if (n < 2) {
            return;
        }
        Vec3[] side = new Vec3[n];
        for (int i = 0; i < n; i++) {
            Vec3 tangent = points[Math.max(i - 1, 0)].subtract(points[Math.min(i + 1, n - 1)]);
            Vec3 s = tangent.cross(camera.subtract(points[i]));
            side[i] = s.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 1.0, 0.0) : s.normalize();
        }
        for (int i = 0; i + 1 < n; i++) {
            Vec3 a = points[i];
            Vec3 b = points[i + 1];
            Vec3 sa = side[i].scale(width[i] * 0.5F);
            Vec3 sb = side[i + 1].scale(width[i + 1] * 0.5F);
            vertex(buffer, pose, a.add(sa), 0.0F, 0.0F, rgb, light[i]);
            vertex(buffer, pose, a.subtract(sa), 0.0F, 1.0F, rgb, light[i]);
            vertex(buffer, pose, b.subtract(sb), 1.0F, 1.0F, rgb, light[i + 1]);
            vertex(buffer, pose, b.add(sb), 1.0F, 0.0F, rgb, light[i + 1]);
        }
    }

    public static void band(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] left, Vec3[] right, float[] light, int[] rgb) {
        for (int i = 0; i + 1 < left.length; i++) {
            vertex(buffer, pose, left[i], 0.0F, 0.0F, rgb[i], light[i]);
            vertex(buffer, pose, right[i], 0.0F, 1.0F, rgb[i], light[i]);
            vertex(buffer, pose, right[i + 1], 1.0F, 1.0F, rgb[i + 1], light[i + 1]);
            vertex(buffer, pose, left[i + 1], 1.0F, 0.0F, rgb[i + 1], light[i + 1]);
        }
    }

    public static void flare(PoseStack.Pose pose, VertexConsumer buffer, Vec3 at, float size, int rgb, float light, Vec3 camera) {
        Vec3 view = camera.subtract(at);
        if (view.lengthSqr() < 1.0E-6) {
            return;
        }
        view = view.normalize();
        Vec3 right = view.cross(new Vec3(0.0, 1.0, 0.0));
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
        Vec3 up = right.cross(view);
        Vec3 r = right.scale(size * 0.5F);
        Vec3 u = up.scale(size * 0.5F);
        vertex(buffer, pose, at.subtract(r).subtract(u), 0.0F, 1.0F, rgb, light);
        vertex(buffer, pose, at.add(r).subtract(u), 1.0F, 1.0F, rgb, light);
        vertex(buffer, pose, at.add(r).add(u), 1.0F, 0.0F, rgb, light);
        vertex(buffer, pose, at.subtract(r).add(u), 0.0F, 0.0F, rgb, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, float u, float v, int rgb, float light) {
        float k = Math.max(0.0F, Math.min(1.0F, light));
        int r = Math.round((rgb >> 16 & 0xFF) * k);
        int g = Math.round((rgb >> 8 & 0xFF) * k);
        int b = Math.round((rgb & 0xFF) * k);
        buffer.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
