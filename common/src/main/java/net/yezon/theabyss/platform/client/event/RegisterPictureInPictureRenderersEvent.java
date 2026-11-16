package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterPictureInPictureRenderersEvent extends Event {
    public abstract <T extends net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState> void register(Class<T> a0, java.util.function.Supplier<net.minecraft.client.gui.render.pip.PictureInPictureRenderer<T>> a1);
}
