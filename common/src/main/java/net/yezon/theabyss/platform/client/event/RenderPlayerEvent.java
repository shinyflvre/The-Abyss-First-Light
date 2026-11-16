package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderPlayerEvent extends Event {

    public static class Post extends RenderPlayerEvent {
        private final com.mojang.blaze3d.vertex.PoseStack poseStack;
        private final net.minecraft.client.renderer.entity.state.AvatarRenderState renderState;

        public Post(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.entity.state.LivingEntityRenderState renderState) {
            this.poseStack = poseStack;
            this.renderState = (net.minecraft.client.renderer.entity.state.AvatarRenderState)renderState;
        }

        public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
            return this.poseStack;
        }

        public net.minecraft.client.renderer.entity.state.AvatarRenderState getRenderState() {
            return this.renderState;
        }
    }

    public static class Pre extends RenderPlayerEvent implements ICancellableEvent {
        private final com.mojang.blaze3d.vertex.PoseStack poseStack;
        private final net.minecraft.client.renderer.entity.state.AvatarRenderState renderState;

        public Pre(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.entity.state.LivingEntityRenderState renderState) {
            this.poseStack = poseStack;
            this.renderState = (net.minecraft.client.renderer.entity.state.AvatarRenderState)renderState;
        }

        public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
            return this.poseStack;
        }

        public net.minecraft.client.renderer.entity.state.AvatarRenderState getRenderState() {
            return this.renderState;
        }
    }
}
