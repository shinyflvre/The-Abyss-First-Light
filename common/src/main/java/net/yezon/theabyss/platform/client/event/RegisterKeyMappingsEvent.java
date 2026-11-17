package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterKeyMappingsEvent extends Event {
    public abstract void register(net.minecraft.client.KeyMapping a0);
}
