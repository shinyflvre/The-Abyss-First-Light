package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.client.model.Modelzombie1;
import net.yezon.theabyss.entity.InfectedZombieEntity;

public class InfectedZombieRenderer extends MobRenderer<InfectedZombieEntity, Modelzombie1<InfectedZombieEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/infected_zombie_skeleton.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/infected_zombie_skeleton_glowing_layer.png"));

    public InfectedZombieRenderer(Context context) {
        super(context, new Modelzombie1<>(context.bakeLayer(Modelzombie1.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedZombieEntity entity) {
        return TEXTURE;
    }
}
