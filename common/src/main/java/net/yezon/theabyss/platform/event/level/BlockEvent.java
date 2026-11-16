package net.yezon.theabyss.platform.event.level;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class BlockEvent extends Event {

    public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.Entity entity;
        private final net.minecraft.world.level.LevelAccessor level;
        private final net.minecraft.core.BlockPos pos;

        public EntityPlaceEvent(net.minecraft.world.entity.Entity entity, net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos) {
            this.entity = entity;
            this.level = level;
            this.pos = pos;
        }

        public net.minecraft.world.entity.Entity getEntity() {
            return this.entity;
        }

        public net.minecraft.world.level.LevelAccessor getLevel() {
            return this.level;
        }

        public net.minecraft.core.BlockPos getPos() {
            return this.pos;
        }
    }
}
