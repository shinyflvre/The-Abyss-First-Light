package net.yezon.theabyss.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.renderer.AbyssRenderTypes;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GlowGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    protected final ResourceLocation texture;

    public GlowGeoLayer(GeoRenderer<T> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }

    protected RenderType glowType(T animatable, float partialTick) {
        return AbyssRenderTypes.eyes(this.texture);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (renderType == null) {
            return;
        }
        RenderType glow = this.glowType(animatable, partialTick);
        if (glow == null) {
            return;
        }
        int color = this.getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt();
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick, packedLight, packedOverlay,
            color);
    }
}
