package net.yezon.theabyss.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererArmInvoker {
    @Invoker("renderPlayerArm")
    void theabyss$renderPlayerArm(PoseStack poseStack, MultiBufferSource buffers, int packedLight, float equippedProgress, float swingProgress, HumanoidArm side);
}
