package net.yezon.theabyss.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class SwordParticleEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.END_SWORD_PT.get(), x, y, z, 40, 15.0, 15.0, 15.0, 0.01);
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 5, 15.0, 15.0, 15.0, 0.01);
        }
    }
}
