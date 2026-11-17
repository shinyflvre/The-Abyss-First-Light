package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.SummonedHollowSeekerEntity;
import net.yezon.theabyss.init.TheabyssModModels;

public class SummonedHollowSeekerRenderer extends HumanoidMobRenderer<SummonedHollowSeekerEntity, HumanoidModel<SummonedHollowSeekerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/firefly_entity.png");

    public SummonedHollowSeekerRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TheabyssModModels.HUMANOID)), 0.0F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(SummonedHollowSeekerEntity entity) {
        return TEXTURE;
    }
}
