package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public class MovementInputUpdateEvent extends Event {
    private final net.minecraft.client.player.ClientInput input;

    public MovementInputUpdateEvent(net.minecraft.client.player.ClientInput input) {
        this.input = input;
    }

    public net.minecraft.client.player.ClientInput getInput() {
        return this.input;
    }
}
