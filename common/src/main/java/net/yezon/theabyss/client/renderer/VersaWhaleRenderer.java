package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.client.model.Modelwhale;
import net.yezon.theabyss.entity.VersaWhaleEntity;

public class VersaWhaleRenderer extends MobRenderer<VersaWhaleEntity, LivingEntityRenderState, Modelwhale<LivingEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/versa_whale.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/versa_whale_glow.png"));

    public VersaWhaleRenderer(Context context) {
        super(context, new Modelwhale<>(context.bakeLayer(Modelwhale.LAYER_LOCATION)), 0.5F);
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

    @Override
    protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(VersaWhaleEntity entity, float partialTicks) {
        float scale = entity.getScale();
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(13.0 * scale, 5.0 * scale, 13.0 * scale);
    }

    @Override
    protected void setupRotations(LivingEntityRenderState state, com.mojang.blaze3d.vertex.PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.rotateDegrees(com.mojang.math.Axis.XP, state.xRot);
    }
}
