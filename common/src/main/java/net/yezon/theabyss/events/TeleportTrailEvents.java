package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;

public class TeleportTrailEvents {
    public static void execute(LevelAccessor world, Entity pearl) {
        if (world instanceof ServerLevel level && pearl != null) {
            RingVisuals.teleportTrail(level, new Vec3(pearl.xo, pearl.yo, pearl.zo), pearl.position());
        }
    }
}
