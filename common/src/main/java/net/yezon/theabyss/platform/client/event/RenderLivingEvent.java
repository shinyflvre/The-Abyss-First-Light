package net.yezon.theabyss.platform.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.platform.client.EntityRenderSuppression;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderLivingEvent extends Event {
    private final LivingEntity entity;
    private final LivingEntityRenderer<?, ?> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    protected RenderLivingEvent(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.entity = entity;
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public LivingEntityRenderer<?, ?> getRenderer() {
        return this.renderer;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return this.multiBufferSource;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public void hideShadowAndFire() {
        EntityRenderSuppression.hide(this.entity);
    }

    public static class Pre extends RenderLivingEvent implements ICancellableEvent {
        public Pre(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    public static class Post extends RenderLivingEvent {
        public Post(LivingEntity entity, LivingEntityRenderer<?, ?> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
