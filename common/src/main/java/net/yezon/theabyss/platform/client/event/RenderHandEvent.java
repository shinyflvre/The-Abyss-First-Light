package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class RenderHandEvent extends Event implements ICancellableEvent {
    private final float equipProgress;
    private final net.minecraft.world.InteractionHand hand;
    private final net.minecraft.world.item.ItemStack itemStack;
    private final int packedLight;
    private final net.minecraft.client.renderer.state.level.PlayerRenderState playerState;
    private final com.mojang.blaze3d.vertex.PoseStack poseStack;
    private final net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector;
    private final float swingProgress;

    public RenderHandEvent(float equipProgress, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack itemStack, int packedLight, net.minecraft.client.renderer.state.level.PlayerRenderState playerState, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, float swingProgress) {
        this.equipProgress = equipProgress;
        this.hand = hand;
        this.itemStack = itemStack;
        this.packedLight = packedLight;
        this.playerState = playerState;
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.swingProgress = swingProgress;
    }

    public float getEquipProgress() {
        return this.equipProgress;
    }

    public net.minecraft.world.InteractionHand getHand() {
        return this.hand;
    }

    public net.minecraft.world.item.ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public net.minecraft.client.renderer.state.level.PlayerRenderState getPlayerState() {
        return this.playerState;
    }

    public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
        return this.poseStack;
    }

    public net.minecraft.client.renderer.SubmitNodeCollector getSubmitNodeCollector() {
        return this.submitNodeCollector;
    }

    public float getSwingProgress() {
        return this.swingProgress;
    }
}
