package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public record HangingDecoratedFeature(Holder<ConfiguredFeature<?, ?>> feature, Optional<HangingPlants> hanging, int radius, int height, Optional<GroundCover> cover) implements AbyssFeature.Config {
    public record GroundCover(BlockState state, float chance, HolderSet<Block> on, int below, int above) {
        public static final Codec<GroundCover> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockState.CODEC.fieldOf("state").forGetter(GroundCover::state),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(GroundCover::chance),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("on").forGetter(GroundCover::on),
            Codec.intRange(0, 16).fieldOf("below").forGetter(GroundCover::below),
            Codec.intRange(0, 16).fieldOf("above").forGetter(GroundCover::above)
        ).apply(i, GroundCover::new));

        void place(WorldGenLevel level, RandomSource random, BlockPos origin, int radius) {
            BlockPos.MutableBlockPos up = new BlockPos.MutableBlockPos();
            for (BlockPos pos : BlockPos.betweenClosed(origin.getX() - radius, origin.getY() - this.below, origin.getZ() - radius,
                origin.getX() + radius, origin.getY() + this.above, origin.getZ() + radius)) {
                if (!level.getBlockState(pos).is(this.on)) {
                    continue;
                }
                up.setWithOffset(pos, 0, 1, 0);
                if (level.getBlockState(up).isAir() && this.state.canSurvive(level, up) && random.nextFloat() < this.chance) {
                    level.setBlock(up, this.state, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    public static final MapCodec<HangingDecoratedFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ConfiguredFeature.CODEC.fieldOf("feature").forGetter(HangingDecoratedFeature::feature),
        HangingPlants.CODEC.optionalFieldOf("hanging").forGetter(HangingDecoratedFeature::hanging),
        Codec.intRange(0, 16).fieldOf("radius").forGetter(HangingDecoratedFeature::radius),
        Codec.intRange(1, 64).fieldOf("height").forGetter(HangingDecoratedFeature::height),
        GroundCover.CODEC.optionalFieldOf("ground_cover").forGetter(HangingDecoratedFeature::cover)
    ).apply(i, HangingDecoratedFeature::new));
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<HangingDecoratedFeature>> HANGING_DECORATED = REGISTRY.register(
        "hanging_decorated", () -> new AbyssFeature<>(CODEC)
    );

    @Override
    public MapCodec<HangingDecoratedFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.value().getFeatures();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!this.feature.value().place(level, chunkGenerator, random, origin)) {
            return false;
        }
        this.hanging.ifPresent(h -> h.decorate(level, random, BlockPos.betweenClosed(
            origin.getX() - this.radius, origin.getY() - 1, origin.getZ() - this.radius,
            origin.getX() + this.radius, origin.getY() + this.height, origin.getZ() + this.radius
        )));
        this.cover.ifPresent(c -> c.place(level, random, origin, this.radius));
        return true;
    }
}
