package net.yezon.theabyss.platform.event.tick;

import net.yezon.theabyss.platform.event.Event;

public abstract class LevelTickEvent extends Event {

    public static class Post extends LevelTickEvent {
        private final net.minecraft.world.level.Level level;

        public Post(net.minecraft.world.level.Level level) {
            this.level = level;
        }

        public net.minecraft.world.level.Level getLevel() {
            return this.level;
        }
    }
}
