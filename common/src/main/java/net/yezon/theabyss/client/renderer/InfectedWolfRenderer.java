package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelabyss_wolf;
import net.yezon.theabyss.entity.InfectedWolfEntity;

public class InfectedWolfRenderer extends MobRenderer<InfectedWolfEntity, LivingEntityRenderState, Modelabyss_wolf<LivingEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/infected_wolf.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/infected_wolf_glow.png"));

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
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
