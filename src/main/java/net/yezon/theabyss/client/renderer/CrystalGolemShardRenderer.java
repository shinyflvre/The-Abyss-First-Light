package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalGolemShardEntity;

public class CrystalGolemShardRenderer extends EntityRenderer<CrystalGolemShardEntity, CrystalGolemShardRenderer.State> {
    private final ItemModelResolver itemModelResolver;
    private ItemStack crystal;

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float spin;
        Vec3 smoothing = Vec3.ZERO;
    }

    public CrystalGolemShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(CrystalGolemShardEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(CrystalGolemShardEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (this.crystal == null) {
            this.crystal = new ItemStack(Items.AMETHYST_CLUSTER);
        }
        this.itemModelResolver.updateForNonLiving(state.item, this.crystal, ItemDisplayContext.FIXED, entity);
        state.spin = (entity.tickCount + partialTick) * 23.0F;
        state.smoothing = entity.clientVelocity().scale(partialTick - 1.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.smoothing.x, state.smoothing.y + 0.3, state.smoothing.z);
        poseStack.rotateDegrees(Axis.XP, state.spin);
        poseStack.rotateDegrees(Axis.YP, state.spin * 0.6F);
        poseStack.scale(1.7F, 1.7F, 1.7F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
