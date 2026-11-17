package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class RenderArmEvent extends Event implements ICancellableEvent {
    private final com.mojang.blaze3d.vertex.PoseStack poseStack;
    private final net.minecraft.client.renderer.MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final net.minecraft.client.player.AbstractClientPlayer player;
    private final net.minecraft.world.entity.HumanoidArm arm;

    public RenderArmEvent(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, int packedLight, net.minecraft.client.player.AbstractClientPlayer player, net.minecraft.world.entity.HumanoidArm arm) {
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.player = player;
        this.arm = arm;
    }

    public net.minecraft.world.entity.HumanoidArm getArm() {
        return this.arm;
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

    public net.minecraft.client.player.AbstractClientPlayer getPlayer() {
        return this.player;
    }
}
