package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.InfectedCreeperEntity;

public class InfectedCreeperRenderer extends MobRenderer<InfectedCreeperEntity, CreeperModel<InfectedCreeperEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/infected_creeper.png");

    public InfectedCreeperRenderer(Context context) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedCreeperEntity entity) {
        return TEXTURE;
    }
}
