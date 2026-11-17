package net.yezon.theabyss.world.features;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public record HangingPlantFeature(HangingPlants hanging) implements AbyssFeature.Config {
    public static final MapCodec<HangingPlantFeature> CODEC = HangingPlants.CODEC.fieldOf("hanging").xmap(HangingPlantFeature::new, HangingPlantFeature::hanging);
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<HangingPlantFeature>> HANGING_PLANT = REGISTRY.register("hanging_plant", () -> new AbyssFeature<>(CODEC));

    @Override
    public MapCodec<HangingPlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos anchor = origin.above();
        if (!level.getBlockState(origin).isAir() || !level.getBlockState(anchor).isFaceSturdy(level, anchor, Direction.DOWN)) {
            return false;
        }
        this.hanging.decorate(level, random, List.of(anchor));
        return true;
    }
}
