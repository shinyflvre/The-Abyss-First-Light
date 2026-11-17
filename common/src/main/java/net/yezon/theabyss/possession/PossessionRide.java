package net.yezon.theabyss.possession;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class PossessionRide {
    private static final ThreadLocal<Entity> SILENT_RIDER = new ThreadLocal<>();

    private PossessionRide() {
    }

    public static boolean isSilent(Entity rider) {
        return rider != null && SILENT_RIDER.get() == rider;
    }

    static boolean start(ServerPlayer player, Mob mob) {
        if (!mob.getType().canSerialize()) {
            return false;
        }
        Entity previous = SILENT_RIDER.get();
        SILENT_RIDER.set(player);
        try {
            return player.startRiding(mob, true);
        } finally {
            if (previous == null) {
                SILENT_RIDER.remove();
            } else {
                SILENT_RIDER.set(previous);
            }
        }
    }
}
