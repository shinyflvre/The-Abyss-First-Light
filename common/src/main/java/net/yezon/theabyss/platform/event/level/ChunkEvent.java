package net.yezon.theabyss.platform.event.level;

import net.yezon.theabyss.platform.event.Event;

public abstract class ChunkEvent extends Event {

    public static class Unload extends ChunkEvent {
        private final net.minecraft.world.level.chunk.ChunkAccess chunk;
        private final net.minecraft.world.level.LevelAccessor level;

        public Unload(net.minecraft.world.level.chunk.ChunkAccess chunk, net.minecraft.world.level.LevelAccessor level) {
            this.chunk = chunk;
            this.level = level;
        }

        public net.minecraft.world.level.chunk.ChunkAccess getChunk() {
            return this.chunk;
        }

        public net.minecraft.world.level.LevelAccessor getLevel() {
            return this.level;
        }
    }
}
