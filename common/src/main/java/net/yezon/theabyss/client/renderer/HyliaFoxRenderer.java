package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelhylia_fox;
import net.yezon.theabyss.entity.HyliaFoxEntity;

public class HyliaFoxRenderer extends MobRenderer<HyliaFoxEntity, Modelhylia_fox<HyliaFoxEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/hylia_fox.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/hylia_fox_glow.png"));

    public HyliaFoxRenderer(Context context) {
        super(context, new Modelhylia_fox<>(context.bakeLayer(Modelhylia_fox.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(HyliaFoxEntity entity) {
        return TEXTURE;
    }
}
