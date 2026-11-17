package net.yezon.theabyss.platform.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.event.Event;

public class MovementInputUpdateEvent extends Event {
    private final Player player;
    private final Input input;

    public MovementInputUpdateEvent(Player player, Input input) {
        this.player = player;
        this.input = input;
    }

    public Player getEntity() {
        return this.player;
    }

    public Input getInput() {
        return this.input;
    }
}
