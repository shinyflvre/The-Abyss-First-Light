package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.client.model.Modelwhale;
import net.yezon.theabyss.entity.VersaWhaleEntity;

public class VersaWhaleRenderer extends MobRenderer<VersaWhaleEntity, Modelwhale<VersaWhaleEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/versa_whale.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/versa_whale_glow.png"));

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
    public ResourceLocation getTextureLocation(VersaWhaleEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(VersaWhaleEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (super.shouldRender(entity, frustum, camX, camY, camZ)) {
            return true;
        }
        if (!entity.shouldRender(camX, camY, camZ) || entity.noCulling) {
            return false;
        }
        float scale = entity.getScale();
        AABB box = entity.getBoundingBoxForCulling().inflate(13.0 * scale, 5.0 * scale, 13.0 * scale).inflate(0.5);
        return !box.hasNaN() && frustum.isVisible(box);
    }

    @Override
    protected void setupRotations(VersaWhaleEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTick)));
    }
}
