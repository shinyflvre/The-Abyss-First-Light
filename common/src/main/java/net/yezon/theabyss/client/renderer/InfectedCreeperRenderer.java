package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.InfectedCreeperEntity;

public class InfectedCreeperRenderer extends MobRenderer<InfectedCreeperEntity, CreeperRenderState, CreeperModel> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/infected_creeper.png");

    public InfectedCreeperRenderer(Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
