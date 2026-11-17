package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.client.model.AbyssChestModel;
import net.yezon.theabyss.platform.client.event.RegisterItemRenderersEvent;

public class AbyssChestSpecialRenderer implements RegisterItemRenderersEvent.ItemRenderer {
    private final String wood;
    private AbyssChestModel model;

    public AbyssChestSpecialRenderer(String wood) {
        this.wood = wood;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (this.model == null) {
            this.model = new AbyssChestModel(Minecraft.getInstance().getEntityModels().bakeLayer(AbyssChestModel.SINGLE));
        }
        this.model.setupAnim(0.0F);
        this.model.renderToBuffer(poseStack, buffers.getBuffer(this.model.renderType(AbyssChestRenderer.texture(this.wood, ChestType.SINGLE))), packedLight,
            packedOverlay);
        this.model.renderToBuffer(poseStack, buffers.getBuffer(AbyssChestRenderer.glow(ChestType.SINGLE)), AbyssChestRenderer.FULL_BRIGHT, packedOverlay);
    }
}
