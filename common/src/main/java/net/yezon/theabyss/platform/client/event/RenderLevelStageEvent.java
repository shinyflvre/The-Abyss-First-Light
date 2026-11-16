package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RenderLevelStageEvent extends Event {

    public static class AfterOpaqueBlocks extends RenderLevelStageEvent {
        private final net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState;
        private final org.joml.Matrix4fc modelViewMatrix;
        private final com.mojang.renderpearl.api.commands.RenderPass renderPass;

        public AfterOpaqueBlocks(net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState, org.joml.Matrix4fc modelViewMatrix, com.mojang.renderpearl.api.commands.RenderPass renderPass) {
            this.levelRenderState = levelRenderState;
            this.modelViewMatrix = modelViewMatrix;
            this.renderPass = renderPass;
        }

        public net.minecraft.client.renderer.state.level.LevelRenderState getLevelRenderState() {
            return this.levelRenderState;
        }

        public org.joml.Matrix4fc getModelViewMatrix() {
            return this.modelViewMatrix;
        }

        public com.mojang.renderpearl.api.commands.RenderPass getRenderPass() {
            return this.renderPass;
        }
    }
}
