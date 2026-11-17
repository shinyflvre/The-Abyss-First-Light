package net.yezon.theabyss.client.renderer;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.entity.MudZombieEntity;
import net.yezon.theabyss.init.TheabyssModModels;

public class MudZombieRenderer extends HumanoidMobRenderer<MudZombieEntity, MudZombieRenderer.Model> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/mud_zombie.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/mud_zombie_glow.png"));

    public MudZombieRenderer(Context context) {
        super(context, new Model(context.bakeLayer(TheabyssModModels.HUMANOID)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new Model(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
            new Model(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(MudZombieEntity entity) {
        return TEXTURE;
    }

    public static class Model extends AbstractZombieModel<MudZombieEntity> {
        public Model(ModelPart root) {
            super(root);
        }

        @Override
        public boolean isAggressive(MudZombieEntity entity) {
            return entity.isAggressive();
        }
    }
}
