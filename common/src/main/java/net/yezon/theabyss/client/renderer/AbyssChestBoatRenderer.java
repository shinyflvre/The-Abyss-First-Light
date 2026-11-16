package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.client.model.AbyssChestModel;

public class AbyssChestBoatRenderer extends BoatRenderer {
    private static final float SCALE = 12.0F / 14.0F;
    private final AbyssChestModel chest;
    private final String wood;

    public AbyssChestBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation hull, String wood) {
        super(context, hull);
        this.chest = new AbyssChestModel(context.bakeLayer(AbyssChestModel.SINGLE));
        this.wood = wood;
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
        super.submitTypeAdditions(state, poseStack, collector, lightCoords);
        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.1875F, 0.0F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        poseStack.rotateDegrees(Axis.ZP, 180.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        collector.submitModel(this.chest, 0.0F, poseStack, this.chest.renderType(AbyssChestRenderer.texture(this.wood, ChestType.SINGLE)),
            lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.order(1).submitModel(this.chest, 0.0F, poseStack, AbyssChestRenderer.glow(ChestType.SINGLE), AbyssChestRenderer.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
