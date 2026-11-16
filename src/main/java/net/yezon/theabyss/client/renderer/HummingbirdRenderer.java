package net.yezon.theabyss.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.HummingbirdEntity;
import net.yezon.theabyss.entity.layer.HummingbirdLayer;
import net.yezon.theabyss.entity.model.HummingbirdModel;
import org.jspecify.annotations.Nullable;

public class HummingbirdRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<HummingbirdEntity, R> {
    public HummingbirdRenderer(Context renderManager) {
        this(renderManager, HummingbirdLayer.INFECTED);
    }

    public HummingbirdRenderer(Context renderManager, Identifier glow) {
        super(renderManager, new HummingbirdModel());
        this.shadowRadius = 0.8F;
        this.withRenderLayer(new HummingbirdLayer<>(this, glow));
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, Identifier texture) {
        return renderState.isInvisible ? super.getRenderType(renderState, texture) : RenderTypes.entityTranslucent(texture);
    }
}
