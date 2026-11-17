package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public final class AbyssFeature<C extends AbyssFeature.Config> extends Feature<C> {
    public AbyssFeature(MapCodec<C> codec) {
        super(codec.codec());
    }

    @Override
    public boolean place(FeaturePlaceContext<C> context) {
        return context.config().place(context.level(), context.chunkGenerator(), context.random(), context.origin());
    }

    public interface Config extends FeatureConfiguration {
        MapCodec<? extends Config> codec();

        boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin);
    }
}
