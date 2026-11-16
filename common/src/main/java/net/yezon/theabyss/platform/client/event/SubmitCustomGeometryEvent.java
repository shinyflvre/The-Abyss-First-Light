package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class SubmitCustomGeometryEvent extends Event {
    private final net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState;
    private final com.mojang.blaze3d.vertex.PoseStack poseStack;
    private final net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector;

    public SubmitCustomGeometryEvent(net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector) {
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
    }

    public net.minecraft.client.renderer.state.level.LevelRenderState getLevelRenderState() {
        return this.levelRenderState;
    }

    public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
        return this.poseStack;
    }

    public net.minecraft.client.renderer.SubmitNodeCollector getSubmitNodeCollector() {
        return this.submitNodeCollector;
    }
}
