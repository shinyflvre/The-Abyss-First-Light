package net.yezon.theabyss.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import net.yezon.theabyss.entity.layer.InfectedDragonflyLayer;
import net.yezon.theabyss.entity.model.InfectedDragonflyModel;
import org.jspecify.annotations.Nullable;

public class InfectedDragonflyRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<InfectedDragonflyEntity, R> {
    public InfectedDragonflyRenderer(Context renderManager) {
        super(renderManager, new InfectedDragonflyModel());
        this.shadowRadius = 0.6F;
        this.withRenderLayer(new InfectedDragonflyLayer<>(this));
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        return renderState.isInvisible ? super.getRenderType(renderState, texture) : RenderTypes.entityTranslucent(texture);
    }
}
