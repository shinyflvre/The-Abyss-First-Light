package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import net.yezon.theabyss.entity.layer.InfectedDragonflyLayer;
import net.yezon.theabyss.entity.model.InfectedDragonflyModel;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class InfectedDragonflyRenderer extends GeoEntityRenderer<InfectedDragonflyEntity> {
    public InfectedDragonflyRenderer(Context renderManager) {
        super(renderManager, new InfectedDragonflyModel());
        this.shadowRadius = 0.6F;
        this.addRenderLayer(new InfectedDragonflyLayer(this));
    }

    @Override
    public @Nullable RenderType getRenderType(InfectedDragonflyEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource,
                                              float partialTick) {
        return animatable.isInvisible() ? super.getRenderType(animatable, texture, bufferSource, partialTick) : RenderType.entityTranslucent(texture);
    }
}
