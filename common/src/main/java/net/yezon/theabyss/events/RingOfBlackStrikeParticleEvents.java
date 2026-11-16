package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;

public class RingOfBlackStrikeParticleEvents {
    public static void execute(LevelAccessor world, Entity bolt) {
        if (world instanceof ServerLevel level && bolt != null) {
            RingVisuals.blackStrikeTrail(level, new Vec3(bolt.xo, bolt.yo, bolt.zo), bolt.position());
        }
    }
}
