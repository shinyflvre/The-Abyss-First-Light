package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;

public abstract class PlayerEvent extends Event {

    public static class Clone extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;
        private final net.minecraft.world.entity.player.Player original;

        public Clone(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.player.Player original) {
            this.entity = entity;
            this.original = original;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public net.minecraft.world.entity.player.Player getOriginal() {
            return this.original;
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to;

        public PlayerChangedDimensionEvent(net.minecraft.world.entity.LivingEntity entity, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to) {
            this.entity = entity;
            this.from = from;
            this.to = to;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getFrom() {
            return this.from;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getTo() {
            return this.to;
        }
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public PlayerLoggedInEvent(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public PlayerLoggedOutEvent(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public PlayerRespawnEvent(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }

    public static class StartTracking extends PlayerEvent {
        private final net.minecraft.world.entity.LivingEntity entity;
        private final net.minecraft.world.entity.Entity target;

        public StartTracking(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.Entity target) {
            this.entity = entity;
            this.target = target;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return this.target;
        }
    }
}
