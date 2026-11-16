package net.yezon.theabyss.platform.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class VanillaGameEvent extends Event implements ICancellableEvent {
    private final net.minecraft.world.entity.Entity cause;

    public VanillaGameEvent(net.minecraft.world.entity.Entity cause) {
        this.cause = cause;
    }

    public net.minecraft.world.entity.Entity getCause() {
        return this.cause;
    }
}
