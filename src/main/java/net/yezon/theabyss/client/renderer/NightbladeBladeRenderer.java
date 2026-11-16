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
import net.yezon.theabyss.entity.NightbladeBladeEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class NightbladeBladeRenderer extends EntityRenderer<NightbladeBladeEntity, NightbladeBladeRenderer.State> {
    private static final float SPIN_PER_TICK = 52.0F;
    private final ItemModelResolver itemModelResolver;
    private ItemStack sword;

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float spin;
        float yaw;
        net.minecraft.world.phys.Vec3 smoothing = net.minecraft.world.phys.Vec3.ZERO;
    }

    public NightbladeBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(NightbladeBladeEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(NightbladeBladeEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (this.sword == null) {
            this.sword = new ItemStack(TheabyssModItems.ABYSS_SWORD.get());
        }
        this.itemModelResolver.updateForNonLiving(state.item, this.sword, ItemDisplayContext.FIXED, entity);
        state.spin = (entity.tickCount + partialTick) * SPIN_PER_TICK;
        var motion = entity.clientVelocity();
        state.smoothing = motion.scale(partialTick - 1.0F);
        state.yaw = motion.horizontalDistanceSqr() > 1.0E-5 ? (float)Math.toDegrees(Math.atan2(motion.x, motion.z)) : 0.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.smoothing.x, state.smoothing.y, state.smoothing.z);
        poseStack.rotateDegrees(Axis.YP, state.yaw);
        poseStack.rotateDegrees(Axis.ZP, 14.0F);
        poseStack.rotateDegrees(Axis.XP, 90.0F);
        poseStack.rotateDegrees(Axis.ZP, state.spin);
        poseStack.scale(1.4F, 1.4F, 1.4F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
