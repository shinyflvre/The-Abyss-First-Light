package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelabyss_wolf;
import net.yezon.theabyss.entity.InfectedWolfEntity;

public class InfectedWolfRenderer extends MobRenderer<InfectedWolfEntity, Modelabyss_wolf<InfectedWolfEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/infected_wolf.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/infected_wolf_glow.png"));

    public InfectedWolfRenderer(Context context) {
        super(context, new Modelabyss_wolf<>(context.bakeLayer(Modelabyss_wolf.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedWolfEntity entity) {
        return TEXTURE;
    }
}
