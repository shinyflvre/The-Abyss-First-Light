package net.yezon.theabyss.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

public class SeekerParticlesEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.01, 0.01, 0.01, 0.01);
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.01, 0.01, 0.01, 0.01);
        }
    }
}
