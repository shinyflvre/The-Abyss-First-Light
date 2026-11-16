package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.flora.TriplePlantBlock;

public record TriplePlantFeature(BlockState state) implements Feature {
    public static final MapCodec<TriplePlantFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BlockState.CODEC.fieldOf("state").forGetter(TriplePlantFeature::state)
    ).apply(i, TriplePlantFeature::new));
    public static final DeferredRegister<MapCodec<? extends Feature>> REGISTRY = DeferredRegister.create(Registries.FEATURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TriplePlantFeature>> TRIPLE_PLANT = REGISTRY.register("triple_plant", () -> CODEC);

    @Override
    public MapCodec<TriplePlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!(this.state.getBlock() instanceof TriplePlantBlock) || !this.state.canSurvive(level, origin)
            || !level.getBlockState(origin.above()).isAir() || !level.getBlockState(origin.above(2)).isAir()) {
            return false;
        }
        TriplePlantBlock.placeAt(level, this.state, origin, 2);
        return true;
    }
}
