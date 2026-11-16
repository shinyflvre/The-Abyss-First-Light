package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.entity.InfcetedCowEntity;
import net.yezon.theabyss.init.TheabyssModModels;

public class InfcetedCowRenderer extends MobRenderer<InfcetedCowEntity, LivingEntityRenderState, CowModel> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/infected_cow.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/infected_cow_glow.png"));

    public InfcetedCowRenderer(Context context) {
        super(context, new CowModel(context.bakeLayer(TheabyssModModels.INFECTED_COW)), 0.5F);
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
