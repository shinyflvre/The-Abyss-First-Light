package net.yezon.theabyss.platform.event.entity.player;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class PlayerInteractEvent extends Event {

    public static class EntityInteract extends PlayerInteractEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.player.Player entity;
        private final net.minecraft.world.item.ItemStack itemStack;
        private final net.minecraft.world.entity.Entity target;

        public EntityInteract(net.minecraft.world.entity.player.Player entity, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.entity.Entity target) {
            this.entity = entity;
            this.itemStack = itemStack;
            this.target = target;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }

        public net.minecraft.world.item.ItemStack getItemStack() {
            return this.itemStack;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return this.target;
        }
    }

    public static class LeftClickBlock extends PlayerInteractEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.player.Player entity;
        private final net.minecraft.world.level.Level level;
        private final net.minecraft.core.BlockPos pos;

        public LeftClickBlock(net.minecraft.world.entity.player.Player entity, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            this.entity = entity;
            this.level = level;
            this.pos = pos;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }

        public net.minecraft.world.level.Level getLevel() {
            return this.level;
        }

        public net.minecraft.core.BlockPos getPos() {
            return this.pos;
        }
    }

    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.player.Player entity;
        private final net.minecraft.world.InteractionHand hand;
        private final net.minecraft.world.item.ItemStack itemStack;
        private final net.minecraft.world.level.Level level;
        private final net.minecraft.core.BlockPos pos;
        private net.yezon.theabyss.compat.TriState useItem;

        public RightClickBlock(net.minecraft.world.entity.player.Player entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.yezon.theabyss.compat.TriState useItem) {
            this.entity = entity;
            this.hand = hand;
            this.itemStack = itemStack;
            this.level = level;
            this.pos = pos;
            this.useItem = useItem;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }

        public net.minecraft.world.InteractionHand getHand() {
            return this.hand;
        }

        public net.minecraft.world.item.ItemStack getItemStack() {
            return this.itemStack;
        }

        public net.minecraft.world.level.Level getLevel() {
            return this.level;
        }

        public net.minecraft.core.BlockPos getPos() {
            return this.pos;
        }

        public net.yezon.theabyss.compat.TriState getUseItem() {
            return this.useItem;
        }

        public void setUseItem(net.yezon.theabyss.compat.TriState value) {
            this.useItem = value;
        }
    }

    public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.player.Player entity;
        private final net.minecraft.world.item.ItemStack itemStack;

        public RightClickItem(net.minecraft.world.entity.player.Player entity, net.minecraft.world.item.ItemStack itemStack) {
            this.entity = entity;
            this.itemStack = itemStack;
        }

        public net.minecraft.world.entity.player.Player getEntity() {
            return this.entity;
        }

        public net.minecraft.world.item.ItemStack getItemStack() {
            return this.itemStack;
        }
    }
}
