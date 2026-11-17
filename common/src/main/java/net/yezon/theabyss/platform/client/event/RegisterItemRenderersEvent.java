package net.yezon.theabyss.platform.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterItemRenderersEvent extends Event {
    public abstract void register(ItemRenderer renderer, ItemLike... items);

    @FunctionalInterface
    public interface ItemRenderer {
        void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay);
    }
}
