package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.yezon.theabyss.entity.PlayerBodyEntity;
import net.yezon.theabyss.entity.model.PlayerBodyModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlayerBodyRenderer extends GeoEntityRenderer<PlayerBodyEntity> {
    public PlayerBodyRenderer(Context context) {
        super(context, new PlayerBodyModel());
        this.shadowRadius = 0.5F;
    }
}
