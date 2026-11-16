package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderLivingEvent extends Event {

    public static class Pre extends RenderLivingEvent implements ICancellableEvent {
        private final com.mojang.blaze3d.vertex.PoseStack poseStack;
        private final net.minecraft.client.renderer.entity.state.LivingEntityRenderState renderState;

        public Pre(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.entity.state.LivingEntityRenderState renderState) {
            this.poseStack = poseStack;
            this.renderState = renderState;
        }

        public com.mojang.blaze3d.vertex.PoseStack getPoseStack() {
            return this.poseStack;
        }

        public net.minecraft.client.renderer.entity.state.LivingEntityRenderState getRenderState() {
            return this.renderState;
        }
    }
}
