package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.client.model.AbyssChestModel;

public class AbyssChestBoatRenderer extends AbyssBoatRenderer {
    private static final float SCALE = 12.0F / 14.0F;
    private final AbyssChestModel chest;
    private final String wood;

    public AbyssChestBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation hull, String wood) {
        super(context, hull);
        this.chest = new AbyssChestModel(context.bakeLayer(AbyssChestModel.SINGLE));
        this.wood = wood;
    }

    @Override
    protected void renderTypeAdditions(Boat entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        super.renderTypeAdditions(entity, partialTicks, poseStack, buffers, light);
        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.1875F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        this.chest.setupAnim(0.0F);
        this.chest.renderToBuffer(poseStack, buffers.getBuffer(this.chest.renderType(AbyssChestRenderer.texture(this.wood, ChestType.SINGLE))), light,
            OverlayTexture.NO_OVERLAY);
        this.chest.renderToBuffer(poseStack, buffers.getBuffer(AbyssChestRenderer.glow(ChestType.SINGLE)), AbyssChestRenderer.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
