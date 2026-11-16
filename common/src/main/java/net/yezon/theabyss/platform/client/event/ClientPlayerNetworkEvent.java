package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class ClientPlayerNetworkEvent extends Event {

    public static class LoggingIn extends ClientPlayerNetworkEvent {
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
    }
}
