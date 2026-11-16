package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;

public abstract class MobSpawnEvent extends Event {

    public static class PositionCheck extends MobSpawnEvent {
        private final net.minecraft.world.entity.Entity entity;
        private final net.minecraft.world.level.ServerLevelAccessor level;
        private final net.minecraft.world.entity.EntitySpawnReason spawnType;
        private net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent.PositionCheck.Result result;

        public PositionCheck(net.minecraft.world.entity.Entity entity, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.EntitySpawnReason spawnType, net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent.PositionCheck.Result result) {
            this.entity = entity;
            this.level = level;
            this.spawnType = spawnType;
            this.result = result;
        }

        public net.minecraft.world.entity.Entity getEntity() {
            return this.entity;
        }

        public net.minecraft.world.level.ServerLevelAccessor getLevel() {
            return this.level;
        }

        public net.minecraft.world.entity.EntitySpawnReason getSpawnType() {
            return this.spawnType;
        }

        public net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent.PositionCheck.Result getResult() {
            return this.result;
        }

        public void setResult(net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent.PositionCheck.Result value) {
            this.result = value;
        }

        public enum Result {
            SUCCEED,
            DEFAULT,
            FAIL
        }
    }
}
