package net.yezon.theabyss.platform.event.level;

import net.yezon.theabyss.platform.event.Event;

public abstract class ExplosionEvent extends Event {

    public static class Detonate extends ExplosionEvent {
        private final java.util.List<net.minecraft.core.BlockPos> affectedBlocks;
        private final net.minecraft.world.level.Level level;

        public Detonate(java.util.List<net.minecraft.core.BlockPos> affectedBlocks, net.minecraft.world.level.Level level) {
            this.affectedBlocks = affectedBlocks;
            this.level = level;
        }

        public java.util.List<net.minecraft.core.BlockPos> getAffectedBlocks() {
            return this.affectedBlocks;
        }

        public net.minecraft.world.level.Level getLevel() {
            return this.level;
        }
    }
}
