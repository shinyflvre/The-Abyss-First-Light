package net.yezon.theabyss.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingFallEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class FallGuard {
    private static final Map<UUID, Long> UNTIL = new ConcurrentHashMap<>();

    private FallGuard() {
    }

    public static void protect(LivingEntity entity, int ticks) {
        UNTIL.put(entity.getUUID(), entity.level().getGameTime() + ticks);
        entity.resetFallDistance();
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        Long until = UNTIL.get(entity.getUUID());
        if (until == null) {
            return;
        }
        if (entity.level().getGameTime() <= until) {
            event.setCanceled(true);
        } else {
            UNTIL.remove(entity.getUUID());
        }
    }
}
