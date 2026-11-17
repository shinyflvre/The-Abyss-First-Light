package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.NightbladeBladeEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class NightbladeBladeRenderer extends EntityRenderer<NightbladeBladeEntity> {
    private static final float SPIN_PER_TICK = 52.0F;
    private final ItemRenderer itemRenderer;
    private ItemStack sword;

    public NightbladeBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(NightbladeBladeEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(NightbladeBladeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (this.sword == null) {
            this.sword = new ItemStack(TheabyssModItems.ABYSS_SWORD.get());
        }
        float spin = (entity.tickCount + partialTick) * SPIN_PER_TICK;
        Vec3 motion = entity.clientVelocity();
        Vec3 smoothing = motion.scale(partialTick - 1.0F);
        float yaw = motion.horizontalDistanceSqr() > 1.0E-5 ? (float)Math.toDegrees(Math.atan2(motion.x, motion.z)) : 0.0F;
        poseStack.pushPose();
        poseStack.translate(smoothing.x, smoothing.y, smoothing.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(14.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        poseStack.scale(1.4F, 1.4F, 1.4F);
        this.itemRenderer.renderStatic(this.sword, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(NightbladeBladeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
