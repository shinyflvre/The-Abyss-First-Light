package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelinfected_jellyfish;
import net.yezon.theabyss.entity.JellyFishEntity;

public class JellyFishRenderer extends MobRenderer<JellyFishEntity, Modelinfected_jellyfish<JellyFishEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/jelly_fish.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/jelly_fish.png"));

    public JellyFishRenderer(Context context) {
        super(context, new Modelinfected_jellyfish<>(context.bakeLayer(Modelinfected_jellyfish.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(JellyFishEntity entity) {
        return TEXTURE;
    }
}
