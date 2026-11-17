package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.yezon.theabyss.block.BlossomRankBlock;
import net.yezon.theabyss.init.TheabyssModBlocks;

public record BlossomRankPatch(int minHeight, int maxHeight, float clusterChance, int clusterMin, int clusterMax, int spread, float bulbChance) implements AbyssFeature.Config {
    public static final MapCodec<BlossomRankPatch> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(1, 16).fieldOf("min_height").forGetter(BlossomRankPatch::minHeight),
            Codec.intRange(1, 16).fieldOf("max_height").forGetter(BlossomRankPatch::maxHeight),
            Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_chance").forGetter(BlossomRankPatch::clusterChance),
            Codec.intRange(1, 16).fieldOf("cluster_min").forGetter(BlossomRankPatch::clusterMin),
            Codec.intRange(1, 16).fieldOf("cluster_max").forGetter(BlossomRankPatch::clusterMax),
            Codec.intRange(0, 8).fieldOf("spread").forGetter(BlossomRankPatch::spread),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("bulb_chance", 0.0F).forGetter(BlossomRankPatch::bulbChance))
        .apply(i, BlossomRankPatch::new));

    @Override
    public MapCodec<BlossomRankPatch> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockState plant = (random.nextFloat() < this.bulbChance ? TheabyssModBlocks.BULB_RANK : TheabyssModBlocks.BLOSSOM_RANK).get().defaultBlockState();
        if (random.nextFloat() >= this.clusterChance) {
            return grow(level, random, origin.getX(), origin.getZ(), plant);
        }
        int wanted = this.clusterMin + random.nextInt(Math.max(1, this.clusterMax - this.clusterMin + 1));
        int placed = 0;
        for (int attempt = 0; attempt < wanted * 3 && placed < wanted; attempt++) {
            int x = origin.getX() + random.nextInt(this.spread * 2 + 1) - this.spread;
            int z = origin.getZ() + random.nextInt(this.spread * 2 + 1) - this.spread;
            if (grow(level, random, x, z, plant)) {
                placed++;
            }
        }
        return placed > 0;
    }

    private static boolean free(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private boolean grow(WorldGenLevel level, RandomSource random, int x, int z, BlockState plant) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
        int floor = Math.max(level.getMinBuildHeight() + 1, pos.getY() - 48);
        while (pos.getY() > floor) {
            BlockState below = level.getBlockState(pos.below());
            if (free(below) || below.is(BlockTags.LEAVES)) {
                pos.move(0, -1, 0);
            } else {
                break;
            }
        }
        if (!BlossomRankBlock.isSoil(level.getBlockState(pos.below()))) {
            return false;
        }
        int height = this.minHeight + random.nextInt(Math.max(1, this.maxHeight - this.minHeight + 1));
        int room = 0;
        while (room < height && free(level.getBlockState(pos.above(room)))) {
            room++;
        }
        if (room < this.minHeight) {
            return false;
        }
        height = room;
        for (int i = 0; i < height; i++) {
            BlossomRankBlock.Segment segment = i == height - 1 ? BlossomRankBlock.Segment.END
                : i == height - 2 ? BlossomRankBlock.Segment.CLOSE_END : i == 0 ? BlossomRankBlock.Segment.START : BlossomRankBlock.Segment.MIDDLE;
            level.setBlock(pos.above(i), plant.setValue(BlossomRankBlock.SEGMENT, segment), 2);
        }
        return true;
    }
}
