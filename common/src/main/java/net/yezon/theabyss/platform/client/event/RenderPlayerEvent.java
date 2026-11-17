package net.yezon.theabyss.platform.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.client.EntityRenderSuppression;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderPlayerEvent extends Event {
    private final Player player;
    private final PlayerRenderer renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    protected RenderPlayerEvent(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.player = player;
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public Player getEntity() {
        return this.player;
    }

    public PlayerRenderer getRenderer() {
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
        EntityRenderSuppression.hide(this.player);
    }

    public static class Pre extends RenderPlayerEvent implements ICancellableEvent {
        public Pre(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    public static class Post extends RenderPlayerEvent {
        public Post(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
