package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.InfcetedCowEntity;
import net.yezon.theabyss.init.TheabyssModModels;

public class InfcetedCowRenderer extends MobRenderer<InfcetedCowEntity, CowModel<InfcetedCowEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/infected_cow.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/infected_cow_glow.png"));

    public InfcetedCowRenderer(Context context) {
        super(context, new CowModel<>(context.bakeLayer(TheabyssModModels.INFECTED_COW)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(InfcetedCowEntity entity) {
        return TEXTURE;
    }
}
