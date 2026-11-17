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
import net.yezon.theabyss.entity.LurkerSpitEntity;
import net.yezon.theabyss.init.TheabyssModItems;

public class LurkerSpitRenderer extends EntityRenderer<LurkerSpitEntity> {
    private final ItemRenderer itemRenderer;
    private ItemStack sobber;

    public LurkerSpitRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(LurkerSpitEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(LurkerSpitEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (this.sobber == null) {
            this.sobber = new ItemStack(TheabyssModItems.LURKER_SOBBER.get());
        }
        float age = entity.tickCount + partialTick;
        float spin = age * 31.0F;
        float wobble = 1.0F + 0.15F * (float)Math.sin(age * 1.3F);
        Vec3 smoothing = entity.clientVelocity().scale(partialTick - 1.0F);
        poseStack.pushPose();
        poseStack.translate(smoothing.x, smoothing.y + 0.1, smoothing.z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        poseStack.scale(1.3F * wobble, 1.3F / wobble, 1.3F);
        this.itemRenderer.renderStatic(this.sobber, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(LurkerSpitEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
