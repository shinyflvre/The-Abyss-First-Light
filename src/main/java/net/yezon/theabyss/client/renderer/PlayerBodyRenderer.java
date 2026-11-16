package net.yezon.theabyss.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.yezon.theabyss.entity.PlayerBodyEntity;
import net.yezon.theabyss.entity.model.PlayerBodyModel;

public class PlayerBodyRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<PlayerBodyEntity, R> {
    public PlayerBodyRenderer(Context context) {
        super(context, new PlayerBodyModel());
        this.shadowRadius = 0.5F;
    }
}
