package net.yezon.theabyss.platform.event.tick;

import net.yezon.theabyss.platform.event.Event;

public abstract class PlayerTickEvent extends Event {

    public static class Post extends PlayerTickEvent {
        private final net.minecraft.world.entity.player.Player entity;

        public Post(net.minecraft.world.entity.player.Player entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }
    }
}
