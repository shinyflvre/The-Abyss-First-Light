package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class ClientTickEvent extends Event {

    public static class Post extends ClientTickEvent {
    }

    public static class Pre extends ClientTickEvent {
    }
}
