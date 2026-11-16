package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class RenderGuiEvent extends Event {

    public static class Post extends RenderGuiEvent {
    }

    public static class Pre extends RenderGuiEvent implements ICancellableEvent {
    }
}
