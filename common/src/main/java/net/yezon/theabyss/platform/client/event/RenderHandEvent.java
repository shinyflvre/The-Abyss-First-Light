package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class RenderHandEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.InteractionHand hand;
    private final com.mojang.blaze3d.vertex.PoseStack poseStack;
    private final net.minecraft.client.renderer.MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final float partialTick;
    private final float interpolatedPitch;
    private final float swingProgress;
    private final float equipProgress;
    private final net.minecraft.world.item.ItemStack itemStack;

    public RenderHandEvent(net.minecraft.world.InteractionHand hand, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, int packedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress, net.minecraft.world.item.ItemStack itemStack) {
        this.hand = hand;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.partialTick = partialTick;
        this.interpolatedPitch = interpolatedPitch;
        this.swingProgress = swingProgress;
        this.equipProgress = equipProgress;
        this.itemStack = itemStack;
    }

    public net.minecraft.world.InteractionHand getHand() {
        return this.hand;
    }

    public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
        return this.poseStack;
    }

    public net.minecraft.client.renderer.MultiBufferSource getMultiBufferSource() {
        return this.multiBufferSource;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public float getInterpolatedPitch() {
        return this.interpolatedPitch;
    }

    public float getSwingProgress() {
        return this.swingProgress;
    }

    public float getEquipProgress() {
        return this.equipProgress;
    }

    public net.minecraft.world.item.ItemStack getItemStack() {
        return this.itemStack;
    }
}
