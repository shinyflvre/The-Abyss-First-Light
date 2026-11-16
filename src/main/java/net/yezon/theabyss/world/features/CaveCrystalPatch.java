package net.yezon.theabyss.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

final class CaveCrystalPatch {
    static final int DEFAULT_TRIES = 128;
    static final int DEFAULT_XZ_SPREAD = 7;
    static final int DEFAULT_Y_SPREAD = 3;

    private CaveCrystalPatch() {
    }

    static boolean place(
        int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature, WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random,
        BlockPos origin
    ) {
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int xz = xzSpread + 1;
        int y = ySpread + 1;
        for (int i = 0; i < tries; i++) {
            pos.setWithOffset(origin, random.nextInt(xz) - random.nextInt(xz), random.nextInt(y) - random.nextInt(y), random.nextInt(xz) - random.nextInt(xz));
            if (feature.value().place(level, chunkGenerator, random, pos.immutable())) {
                placed++;
            }
        }
        return placed > 0;
    }
}
