package net.yezon.theabyss.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yezon.theabyss.entity.HummingbirdEntity;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;

@EventBusSubscriber
public class EntityAnimationFactory {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof InfectedDragonflyEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
                syncable.setAnimation("undefined");
                syncable.animationEvents = animation;
            }
        }

        if (event.getEntity() instanceof HummingbirdEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
                syncable.setAnimation("undefined");
                syncable.animationEvents = animation;
            }
        }
    }
}
