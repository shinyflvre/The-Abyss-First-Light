package net.yezon.theabyss.platform.network.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.network.registration.PayloadRegistrar;

public abstract class RegisterPayloadHandlersEvent extends Event {
    public abstract PayloadRegistrar registrar(String version);
}
