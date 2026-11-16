package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;

public class PlayerWakeUpEvent extends Event {
    private final net.minecraft.world.entity.player.Player entity;

    public PlayerWakeUpEvent(net.minecraft.world.entity.player.Player entity) {
        this.entity = entity;
    }

    public net.minecraft.world.entity.player.Player getEntity() {
        return this.entity;
    }
}
