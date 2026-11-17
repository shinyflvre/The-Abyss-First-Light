package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.entity.JungleLurkerEntity;
import net.yezon.theabyss.entity.layer.GlowGeoLayer;
import net.yezon.theabyss.entity.model.LurkerModel;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class JungleLurkerRenderer extends GeoEntityRenderer<JungleLurkerEntity> {
    private static final ResourceLocation GLOW = ResourceLocation.parse("theabyss:textures/entity/lurker_glow.png");
    private static final float SHADOW = 0.5F;

    public JungleLurkerRenderer(Context context) {
        super(context, new LurkerModel());
        this.shadowRadius = SHADOW;
        this.addRenderLayer(new GlowGeoLayer<>(this, GLOW) {
            @Override
            protected @Nullable RenderType glowType(JungleLurkerEntity animatable, float partialTick) {
                float camo = animatable.camouflage(partialTick);
                if (camo > 0.55F) {
                    float age = animatable.tickCount + partialTick;
                    return Mth.sin(age * 0.37F) * Mth.sin(age * 0.11F) > 0.82F ? super.glowType(animatable, partialTick) : null;
                }
                return super.glowType(animatable, partialTick);
            }
        });
    }

    @Override
    protected float getShadowRadius(JungleLurkerEntity entity) {
        return SHADOW * (1.0F - entity.camouflage(RenderTicks.partialTick(entity)));
    }

    @Override
    public @Nullable RenderType getRenderType(JungleLurkerEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.camouflage(partialTick) > 0.01F ? RenderType.entityTranslucent(texture) : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public Color getRenderColor(JungleLurkerEntity animatable, float partialTick, int packedLight) {
        Color base = super.getRenderColor(animatable, partialTick, packedLight);
        float camo = animatable.camouflage(partialTick);
        if (camo <= 0.01F) {
            return base;
        }
        int color = base.argbInt();
        float age = animatable.tickCount + partialTick;
        float shimmer = 0.75F + 0.25F * Mth.sin(age * 0.45F) * Mth.sin(age * 0.17F + 1.3F);
        float alpha = (1.0F - 0.9F * camo) * (camo > 0.5F ? shimmer : 1.0F);
        int r = Mth.lerpInt(camo, 255, 175);
        int g = Mth.lerpInt(camo, 255, 238);
        int b = Mth.lerpInt(camo, 255, 245);
        return new Color(ARGB.color(Mth.clamp(Math.round(ARGB.alpha(color) * alpha), 8, 255), r, g, b));
    }
}
