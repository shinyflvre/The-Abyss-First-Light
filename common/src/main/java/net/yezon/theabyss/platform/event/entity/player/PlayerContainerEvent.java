package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;

public abstract class PlayerContainerEvent extends Event {

    public static class Open extends PlayerContainerEvent {
        private final net.minecraft.world.entity.player.Player entity;

        public Open(net.minecraft.world.entity.player.Player entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }
    }
}
