package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class ExtractLevelRenderStateEvent extends Event {
    private final net.minecraft.client.Camera camera;
    private final net.minecraft.client.DeltaTracker deltaTracker;
    private final net.minecraft.client.renderer.culling.Frustum frustum;
    private final net.minecraft.client.multiplayer.ClientLevel level;
    private final net.minecraft.client.renderer.state.OptionsRenderState optionsRenderState;
    private final net.minecraft.client.renderer.state.level.LevelRenderState renderState;

    public ExtractLevelRenderStateEvent(net.minecraft.client.Camera camera, net.minecraft.client.DeltaTracker deltaTracker, net.minecraft.client.renderer.culling.Frustum frustum, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.client.renderer.state.OptionsRenderState optionsRenderState, net.minecraft.client.renderer.state.level.LevelRenderState renderState) {
        this.camera = camera;
        this.deltaTracker = deltaTracker;
        this.frustum = frustum;
        this.level = level;
        this.optionsRenderState = optionsRenderState;
        this.renderState = renderState;
    }

    public net.minecraft.client.Camera getCamera() {
        return this.camera;
    }

    public net.minecraft.client.DeltaTracker getDeltaTracker() {
        return this.deltaTracker;
    }

    public net.minecraft.client.renderer.culling.Frustum getFrustum() {
        return this.frustum;
    }

    public net.minecraft.client.multiplayer.ClientLevel getLevel() {
        return this.level;
    }

    public net.minecraft.client.renderer.state.OptionsRenderState getOptionsRenderState() {
        return this.optionsRenderState;
    }

    public net.minecraft.client.renderer.state.level.LevelRenderState getRenderState() {
        return this.renderState;
    }
}
