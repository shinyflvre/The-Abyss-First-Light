package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yezon.theabyss.client.model.Modelmagic;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;

public class RingOfFireStrikeAttackProjectileRenderer extends EntityRenderer<RingOfFireStrikeAttackProjectileEntity> {
    private static final ResourceLocation texture = ResourceLocation.parse("theabyss:textures/entity/fire_strike.png");
    private final Modelmagic<RingOfFireStrikeAttackProjectileEntity> model;

    public RingOfFireStrikeAttackProjectileRenderer(Context context) {
        super(context);
        this.model = new Modelmagic<>(context.bakeLayer(Modelmagic.LAYER_LOCATION));
    }

    @Override
    public void render(RingOfFireStrikeAttackProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                       int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((partialTicks == 1.0F ? entity.getYRot() : Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F + entity.getViewXRot(partialTicks)));
        this.model.root().render(poseStack, buffers.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(RingOfFireStrikeAttackProjectileEntity entity) {
        return texture;
    }
}
