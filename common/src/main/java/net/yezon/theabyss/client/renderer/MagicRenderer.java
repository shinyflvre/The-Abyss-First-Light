package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelmagic;
import net.yezon.theabyss.entity.MagicEntity;

public class MagicRenderer extends MobRenderer<MagicEntity, LivingEntityRenderState, Modelmagic<LivingEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/magic.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/magic.png"));

    public MagicRenderer(Context context) {
        super(context, new Modelmagic<>(context.bakeLayer(Modelmagic.LAYER_LOCATION)), 0.6F);
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
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(3.0F, 3.0F, 3.0F);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
