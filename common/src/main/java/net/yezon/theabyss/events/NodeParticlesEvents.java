package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class NodeParticlesEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            _level.sendParticles(TheabyssModParticleTypes.NODE_PARTICLE.get(), x, y, z, 1, 0.1, 0.1, 0.1, 0.2);
        }
    }
}
