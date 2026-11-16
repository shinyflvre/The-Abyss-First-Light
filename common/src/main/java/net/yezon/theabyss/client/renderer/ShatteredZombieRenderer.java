package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelzombie1;
import net.yezon.theabyss.entity.ShatteredZombieEntity;

public class ShatteredZombieRenderer extends MobRenderer<ShatteredZombieEntity, ZombieRenderState, Modelzombie1<ZombieRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/infected_zombie_skeleton.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/infected_zombie_skeleton_glowing_layer.png"));

    public ShatteredZombieRenderer(Context context) {
        super(context, new Modelzombie1<>(context.bakeLayer(Modelzombie1.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(ShatteredZombieEntity entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.swingAnimation = entity.getSwingAnimation(partialTicks);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
