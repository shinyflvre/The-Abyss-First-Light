package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class TextureAtlasStitchedEvent extends Event {
    private final net.minecraft.client.renderer.texture.TextureAtlas atlas;

    public TextureAtlasStitchedEvent(net.minecraft.client.renderer.texture.TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public net.minecraft.client.renderer.texture.TextureAtlas getAtlas() {
        return this.atlas;
    }
}
