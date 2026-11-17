package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.HummingbirdEntity;
import net.yezon.theabyss.entity.layer.HummingbirdLayer;
import net.yezon.theabyss.entity.model.HummingbirdModel;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HummingbirdRenderer extends GeoEntityRenderer<HummingbirdEntity> {
    public HummingbirdRenderer(Context renderManager) {
        this(renderManager, HummingbirdLayer.INFECTED);
    }

    public HummingbirdRenderer(Context renderManager, ResourceLocation glow) {
        super(renderManager, new HummingbirdModel());
        this.shadowRadius = 0.8F;
        this.addRenderLayer(new HummingbirdLayer(this, glow));
    }

    @Override
    public @Nullable RenderType getRenderType(HummingbirdEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isInvisible() ? super.getRenderType(animatable, texture, bufferSource, partialTick) : RenderType.entityTranslucent(texture);
    }
}
