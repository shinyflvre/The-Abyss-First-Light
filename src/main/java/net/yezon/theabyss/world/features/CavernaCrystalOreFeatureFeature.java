package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.yezon.theabyss.events.CaveMossGenEvents;

public record CavernaCrystalOreFeatureFeature(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements Feature {
    public static final MapCodec<CavernaCrystalOreFeatureFeature> CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("tries", CaveCrystalPatch.DEFAULT_TRIES).forGetter(CavernaCrystalOreFeatureFeature::tries),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("xz_spread", CaveCrystalPatch.DEFAULT_XZ_SPREAD).forGetter(CavernaCrystalOreFeatureFeature::xzSpread),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("y_spread", CaveCrystalPatch.DEFAULT_Y_SPREAD).forGetter(CavernaCrystalOreFeatureFeature::ySpread),
                PlacedFeature.CODEC.fieldOf("feature").forGetter(CavernaCrystalOreFeatureFeature::feature)
            )
            .apply(i, CavernaCrystalOreFeatureFeature::new)
    );

    @Override
    public MapCodec<CavernaCrystalOreFeatureFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return this.feature.value().getFeatures();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!CaveMossGenEvents.execute(origin.getY())) {
            return false;
        }
        return CaveCrystalPatch.place(this.tries, this.xzSpread, this.ySpread, this.feature, level, chunkGenerator, random, origin);
    }
}
