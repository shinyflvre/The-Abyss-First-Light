package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelelder_entity;
import net.yezon.theabyss.entity.ElderEntity;

public class ElderRenderer extends MobRenderer<ElderEntity, LivingEntityRenderState, Modelelder_entity<LivingEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/elder.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/elder_glow.png"));

    public ElderRenderer(Context context) {
        super(context, new Modelelder_entity<>(context.bakeLayer(Modelelder_entity.LAYER_LOCATION)), 0.5F);
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
