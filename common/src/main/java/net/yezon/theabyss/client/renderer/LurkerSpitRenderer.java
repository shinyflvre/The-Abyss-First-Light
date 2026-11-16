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
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.LurkerSpitEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class LurkerSpitRenderer extends EntityRenderer<LurkerSpitEntity, LurkerSpitRenderer.State> {
    private final ItemModelResolver itemModelResolver;
    private ItemStack sobber;

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float spin;
        float wobble;
        Vec3 smoothing = Vec3.ZERO;
    }

    public LurkerSpitRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(LurkerSpitEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(LurkerSpitEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (this.sobber == null) {
            this.sobber = new ItemStack(TheabyssModItems.LURKER_SOBBER.get());
        }
        this.itemModelResolver.updateForNonLiving(state.item, this.sobber, ItemDisplayContext.GROUND, entity);
        float age = entity.tickCount + partialTick;
        state.spin = age * 31.0F;
        state.wobble = 1.0F + 0.15F * (float)Math.sin(age * 1.3F);
        state.smoothing = entity.clientVelocity().scale(partialTick - 1.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.smoothing.x, state.smoothing.y + 0.1, state.smoothing.z);
        poseStack.rotate(camera.orientation);
        poseStack.rotateDegrees(Axis.ZP, state.spin);
        poseStack.scale(1.3F * state.wobble, 1.3F / state.wobble, 1.3F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
