package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class RenderArmEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.HumanoidArm arm;
    private final int lightCoords;
    private final net.minecraft.client.renderer.state.level.PlayerRenderState playerRenderState;
    private final com.mojang.blaze3d.vertex.PoseStack poseStack;
    private final net.minecraft.resources.Identifier skinTexture;
    private final net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector;
    private final boolean hasSleeve;

    public RenderArmEvent(net.minecraft.world.entity.HumanoidArm arm, int lightCoords, net.minecraft.client.renderer.state.level.PlayerRenderState playerRenderState, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.resources.Identifier skinTexture, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, boolean hasSleeve) {
        this.arm = arm;
        this.lightCoords = lightCoords;
        this.playerRenderState = playerRenderState;
        this.poseStack = poseStack;
        this.skinTexture = skinTexture;
        this.submitNodeCollector = submitNodeCollector;
        this.hasSleeve = hasSleeve;
    }

    public net.minecraft.world.entity.HumanoidArm getArm() {
        return this.arm;
    }

    public int getLightCoords() {
        return this.lightCoords;
    }

    public net.minecraft.client.renderer.state.level.PlayerRenderState getPlayerRenderState() {
        return this.playerRenderState;
    }

    public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
        return this.poseStack;
    }

    public net.minecraft.resources.Identifier getSkinTexture() {
        return this.skinTexture;
    }

    public net.minecraft.client.renderer.SubmitNodeCollector getSubmitNodeCollector() {
        return this.submitNodeCollector;
    }

    public boolean hasSleeve() {
        return this.hasSleeve;
    }
}
