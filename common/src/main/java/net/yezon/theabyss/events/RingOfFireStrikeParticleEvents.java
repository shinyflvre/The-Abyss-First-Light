package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;

public class RingOfFireStrikeParticleEvents {
    public static final String SCATTER = "theabyss_scatter";

    public static void execute(LevelAccessor world, Entity bolt) {
        if (world instanceof ServerLevel level && bolt != null) {
            if (bolt.entityTags().contains(SCATTER)) {
                Entity owner = bolt instanceof net.minecraft.world.entity.projectile.Projectile p ? p.getOwner() : null;
                if (owner == null || owner.distanceToSqr(bolt) > 25.0) {
                    net.yezon.theabyss.ring.fx.RingFx.line(level, RingVisuals.flame(RingVisuals.FIRE, 0.16F, 8), new Vec3(bolt.xo, bolt.yo, bolt.zo), bolt.position(), 0.9);
                }
                return;
            }
            RingVisuals.fireStrikeTrail(level, new Vec3(bolt.xo, bolt.yo, bolt.zo), bolt.position());
        }
    }
}
