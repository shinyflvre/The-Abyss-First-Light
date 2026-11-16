package net.yezon.theabyss.platform.event.tick;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class EntityTickEvent extends Event {

    public static class Post extends EntityTickEvent {
        private final net.minecraft.world.entity.Entity entity;

        public Post(net.minecraft.world.entity.Entity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.Entity getEntity() {
            return this.entity;
        }
    }

    public static class Pre extends EntityTickEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.Entity entity;

        public Pre(net.minecraft.world.entity.Entity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.Entity getEntity() {
            return this.entity;
        }
    }
}
