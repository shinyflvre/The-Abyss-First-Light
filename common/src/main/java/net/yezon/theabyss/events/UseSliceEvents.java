package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;

public final class UseSliceEvents {
    private static final float CHANCE = 0.4F;
    private static final float SLICE_DAMAGE = 2.0F;
    private static final int[] HIT_DELAYS = {5, 10, 15, 20};
    private static final int[] SOUND_DELAYS = {7, 12, 18};

    private UseSliceEvents() {
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity target) {
        if (!(world instanceof ServerLevel level) || target == null || level.getRandom().nextFloat() >= CHANCE) {
            return;
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 1.0, 2.0, 1.0, 0.1);
        for (int delay : HIT_DELAYS) {
            TheabyssMod.queueServerWork(delay, () -> {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 2, 1.0, 2.0, 1.0, 0.1);
                if (target.isAlive()) {
                    target.hurt(level.damageSources().generic(), SLICE_DAMAGE);
                }
                playSliceSound(level, x, y, z);
            });
        }
        for (int delay : SOUND_DELAYS) {
            TheabyssMod.queueServerWork(delay, () -> playSliceSound(level, x, y, z));
        }
    }

    private static void playSliceSound(ServerLevel level, double x, double y, double z) {
        level.playSound(null, BlockPos.containing(x, y, z), TheabyssModSounds.BLADE_SLICES.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
