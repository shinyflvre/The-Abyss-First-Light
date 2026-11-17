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
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalGolemShardEntity;

public class CrystalGolemShardRenderer extends EntityRenderer<CrystalGolemShardEntity> {
    private final ItemRenderer itemRenderer;
    private ItemStack crystal;

    public CrystalGolemShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(CrystalGolemShardEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(CrystalGolemShardEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (this.crystal == null) {
            this.crystal = new ItemStack(Items.AMETHYST_CLUSTER);
        }
        float spin = (entity.tickCount + partialTick) * 23.0F;
        Vec3 smoothing = entity.clientVelocity().scale(partialTick - 1.0F);
        poseStack.pushPose();
        poseStack.translate(smoothing.x, smoothing.y + 0.3, smoothing.z);
        poseStack.mulPose(Axis.XP.rotationDegrees(spin));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin * 0.6F));
        poseStack.scale(1.7F, 1.7F, 1.7F);
        this.itemRenderer.renderStatic(this.crystal, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CrystalGolemShardEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
