package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;

public abstract class LivingEvent extends Event {

    public static abstract class LivingVisibilityEvent extends LivingEvent {
        public abstract net.minecraft.world.entity.Entity getEntity();
        public abstract void modifyVisibility(double a0);
    }
}
