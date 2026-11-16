package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class LivingEntityUseItemEvent extends Event {

    public static class Finish extends LivingEntityUseItemEvent {
        private final net.minecraft.world.entity.LivingEntity entity;
        private final net.minecraft.world.item.ItemStack item;

        public Finish(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack item) {
            this.entity = entity;
            this.item = item;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public net.minecraft.world.item.ItemStack getItem() {
            return this.item;
        }
    }

    public static class Start extends LivingEntityUseItemEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public Start(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }
}
