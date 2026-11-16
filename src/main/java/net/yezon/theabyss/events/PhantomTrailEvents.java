package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class PhantomTrailEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 4, 0.005, 0.005, 0.005, 0.1);
        }

        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.PHANTOM_FX.get(), x, y, z, 2, 0.002, 0.002, 0.002, 0.2);
        }
    }
}
