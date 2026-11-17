package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

public class AbyssBoatRenderer extends EntityRenderer<Boat> {
    protected final ResourceLocation texture;
    protected final BoatModel model;

    public AbyssBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation hull) {
        super(context);
        this.shadowRadius = 0.8F;
        this.texture = hull.getModel().withPath(p -> "textures/entity/" + p + ".png");
        this.model = new BoatModel(context.bakeLayer(hull));
    }

    @Override
    public void render(Boat entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float yRot = partialTicks == 1.0F ? entity.getYRot() : Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        float hurt = entity.getHurtTime() - partialTicks;
        float damage = Math.max(entity.getDamage() - partialTicks, 0.0F);
        if (hurt > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * damage / 10.0F * entity.getHurtDir()));
        }
        float bubble = entity.getBubbleAngle(partialTicks);
        boolean underWater = entity.isUnderWater();
        if (!underWater && !Mth.equal(bubble, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(bubble * (float)(Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        this.model.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, buffers.getBuffer(this.model.renderType(this.texture)), light, OverlayTexture.NO_OVERLAY);
        this.renderTypeAdditions(entity, partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
    }

    protected void renderTypeAdditions(Boat entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (!entity.isUnderWater()) {
            this.model.waterPatch().render(poseStack, buffers.getBuffer(RenderType.waterMask()), light, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Boat entity) {
        return this.texture;
    }
}
