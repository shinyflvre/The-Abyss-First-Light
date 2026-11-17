package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.NightBladeBossCloneEntity;
import net.yezon.theabyss.init.TheabyssModModels;

public class NightBladeBossCloneRenderer extends HumanoidMobRenderer<NightBladeBossCloneEntity, HumanoidModel<NightBladeBossCloneEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/firefly_entity.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/nightblade_glowing_layer.png"));

    public NightBladeBossCloneRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TheabyssModModels.HUMANOID)), 0.7F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(NightBladeBossCloneEntity entity) {
        return TEXTURE;
    }
}
