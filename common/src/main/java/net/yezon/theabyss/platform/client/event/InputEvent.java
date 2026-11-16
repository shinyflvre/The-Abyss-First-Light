package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class InputEvent extends Event {

    public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {
        private final net.minecraft.world.InteractionHand hand;
        private final boolean attack;
        private final boolean pickBlock;
        private final boolean useItem;
        private boolean swingHand;

        public InteractionKeyMappingTriggered(net.minecraft.world.InteractionHand hand, boolean attack, boolean pickBlock, boolean useItem, boolean swingHand) {
            this.hand = hand;
            this.attack = attack;
            this.pickBlock = pickBlock;
            this.useItem = useItem;
            this.swingHand = swingHand;
        }

        public net.minecraft.world.InteractionHand getHand() {
            return this.hand;
        }

        public boolean isAttack() {
            return this.attack;
        }

        public boolean isPickBlock() {
            return this.pickBlock;
        }

        public boolean isUseItem() {
            return this.useItem;
        }

        public boolean getSwingHand() {
            return this.swingHand;
        }

        public void setSwingHand(boolean value) {
            this.swingHand = value;
        }
    }

    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {
        private final double scrollDeltaY;

        public MouseScrollingEvent(double scrollDeltaY) {
            this.scrollDeltaY = scrollDeltaY;
        }

        public double getScrollDeltaY() {
            return this.scrollDeltaY;
        }
    }
}
