package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonHandsEventsMixin {
    @WrapOperation(method = "renderHandsWithItems", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void theabyss$renderHand(ItemInHandRenderer renderer, AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
        float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffers, int packedLight, Operation<Void> original) {
        if (ClientFabricEvents.renderHand(hand, poseStack, buffers, packedLight, partialTick, pitch, swingProgress, equipProgress, stack)) {
            return;
        }
        original.call(renderer, player, partialTick, pitch, hand, swingProgress, stack, equipProgress, poseStack, buffers, packedLight);
    }
}
