package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsEventsMixin {
    @WrapOperation(method = "submitHandsWithItems", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void theabyss$renderHand(FirstPersonHandsAndItemsRenderer renderer, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state,
        float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack item, float inverseArmHeight, PoseStack poseStack,
        SubmitNodeCollector collector, int light, Operation<Void> original) {
        if (ClientFabricEvents.renderHand(inverseArmHeight, hand, item, playerState, poseStack, collector, attack)) {
            return;
        }
        original.call(renderer, playerState, state, partialTicks, xRot, hand, attack, item, inverseArmHeight, poseStack, collector, light);
    }

    @WrapOperation(method = "renderPlayerHand", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void theabyss$renderRightArm(AvatarRenderer<?> avatar, PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve,
        Operation<Void> original, @Local(argsOnly = true) PlayerRenderState playerState) {
        if (ClientFabricEvents.renderArm(HumanoidArm.RIGHT, light, playerState, poseStack, skin, collector, sleeve)) {
            return;
        }
        original.call(avatar, poseStack, collector, light, skin, sleeve);
    }

    @WrapOperation(method = "renderPlayerHand", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void theabyss$renderLeftArm(AvatarRenderer<?> avatar, PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve,
        Operation<Void> original, @Local(argsOnly = true) PlayerRenderState playerState) {
        if (ClientFabricEvents.renderArm(HumanoidArm.LEFT, light, playerState, poseStack, skin, collector, sleeve)) {
            return;
        }
        original.call(avatar, poseStack, collector, light, skin, sleeve);
    }
}
