package net.yezon.theabyss.events;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.TheabyssMod;

public class SummonedSeekerOnEntityTickUpdateEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.SMOKE, x, y + 1.0, z, 3, 0.01, 0.01, 0.01, 0.01);
            }

            if (world instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 1, 0.01, 0.01, 0.01, 0.01);
            }

            TheabyssMod.queueServerWork(300, () -> {
                if (!entity.level().isClientSide()) {
                    entity.discard();
                }
            });
        }
    }
}
