package net.yezon.theabyss.events;

import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class ArenoParticlesEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        world.addParticle(TheabyssModParticleTypes.NODE_PARTICLE.get(), x, y, z, 0.0, 1.0, 0.0);
    }
}
