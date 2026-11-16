package net.yezon.theabyss.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class FluidSprings {
    private FluidSprings() {
    }

    public static void pop(Level level, BlockPos pos, RandomSource random, ParticleOptions ember, ParticleOptions splash) {
        BlockPos above = pos.above();
        BlockState state = level.getBlockState(above);
        if (!state.isAir() || state.isSolidRender() || random.nextInt(100) != 0) {
            return;
        }
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ember, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(splash, x, y, z, 0.0, 0.0, 0.0);
    }
}
