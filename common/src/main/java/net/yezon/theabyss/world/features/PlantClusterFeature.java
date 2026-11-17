package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public record PlantClusterFeature(BlockState tall, BlockState plant, IntProvider count, IntProvider tallCount, int spread) implements AbyssFeature.Config {
    public static final MapCodec<PlantClusterFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BlockState.CODEC.fieldOf("tall").forGetter(PlantClusterFeature::tall),
        BlockState.CODEC.fieldOf("plant").forGetter(PlantClusterFeature::plant),
        IntProvider.codec(1, 32).fieldOf("count").forGetter(PlantClusterFeature::count),
        IntProvider.codec(1, 32).fieldOf("tall_count").forGetter(PlantClusterFeature::tallCount),
        Codec.intRange(1, 6).fieldOf("spread").forGetter(PlantClusterFeature::spread)
    ).apply(i, PlantClusterFeature::new));
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, TheabyssMod.MODID);
    public static final DeferredHolder<Feature<?>, AbyssFeature<PlantClusterFeature>> PLANT_CLUSTER = REGISTRY.register("plant_cluster", () -> new AbyssFeature<>(CODEC));

    private record Spot(BlockPos pos, float key) {
    }

    @Override
    public MapCodec<PlantClusterFeature> codec() {
        return CODEC;
    }

    private static @Nullable BlockPos ground(WorldGenLevel level, int x, int z, int top, int bottom) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        while (pos.getY() >= bottom) {
            if (level.isEmptyBlock(pos) && !level.isEmptyBlock(pos.below())) {
                return pos.immutable();
            }
            pos.move(Direction.DOWN);
        }
        return null;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!(this.tall.getBlock() instanceof DoublePlantBlock)) {
            return false;
        }
        List<Spot> spots = new ArrayList<>();
        int reach = this.spread * this.spread + 1;
        for (int dx = -this.spread; dx <= this.spread; dx++) {
            for (int dz = -this.spread; dz <= this.spread; dz++) {
                int d = dx * dx + dz * dz;
                if (d > reach) {
                    continue;
                }
                BlockPos pos = ground(level, origin.getX() + dx, origin.getZ() + dz, origin.getY() + 2, origin.getY() - 3);
                if (pos != null) {
                    spots.add(new Spot(pos, d + random.nextFloat() * 2.5F));
                }
            }
        }
        spots.sort(Comparator.comparingDouble(Spot::key));
        int wanted = this.count.sample(random);
        int talls = Math.min(this.tallCount.sample(random), wanted);
        int placed = 0;
        List<BlockPos> rest = new ArrayList<>();
        for (Spot spot : spots) {
            BlockPos pos = spot.pos();
            if (placed < talls && this.tall.canSurvive(level, pos) && level.isEmptyBlock(pos.above())) {
                DoublePlantBlock.placeAt(level, this.tall, pos, Block.UPDATE_CLIENTS);
                placed++;
            } else {
                rest.add(pos);
            }
        }
        if (placed == 0) {
            return false;
        }
        for (BlockPos pos : rest) {
            if (placed >= wanted) {
                break;
            }
            if (level.isEmptyBlock(pos) && this.plant.canSurvive(level, pos)) {
                level.setBlock(pos, this.plant, Block.UPDATE_CLIENTS);
                placed++;
            }
        }
        return true;
    }
}
