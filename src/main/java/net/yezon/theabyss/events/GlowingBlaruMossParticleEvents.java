package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class GlowingBlaruMossParticleEvents {
    public static void execute(LevelAccessor world, double y, Entity entity) {
        if (entity != null) {
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.RENDER_CYAN.get(), entity.getX(), y, entity.getZ(), 1, 1.0, 0.01, 1.0, 0.01);
            }

            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.RENDER_BRIGHT_CYAN.get(), entity.getX(), y, entity.getZ(), 1, 1.0, 0.01, 1.0, 0.01);
            }

            if (world instanceof ServerLevel _level) {
                _level.sendParticles(TheabyssModParticleTypes.RENDER_LIGHT_CYAN.get(), entity.getX(), y, entity.getZ(), 1, 1.0, 0.01, 1.0, 0.01);
            }
        }
    }
}
