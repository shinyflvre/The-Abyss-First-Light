package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootTable;

public record SurfaceChest(Block block, ResourceKey<LootTable> lootTable, int spiresMin, int spiresMax, int spireMinHeight, int spireMaxHeight)
    implements Feature {
    public static final MapCodec<SurfaceChest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(SurfaceChest::block),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(SurfaceChest::lootTable),
            Codec.INT.optionalFieldOf("spires_min", 5).forGetter(SurfaceChest::spiresMin),
            Codec.INT.optionalFieldOf("spires_max", 9).forGetter(SurfaceChest::spiresMax),
            Codec.INT.optionalFieldOf("spire_min_height", 2).forGetter(SurfaceChest::spireMinHeight),
            Codec.INT.optionalFieldOf("spire_max_height", 7).forGetter(SurfaceChest::spireMaxHeight))
        .apply(i, SurfaceChest::new));

    @Override
    public MapCodec<SurfaceChest> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir() || !DripstoneSpires.rock(ground)
            || !ground.isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        BlockState chest = this.block.defaultBlockState();
        if (chest.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            chest = chest.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
        }
        level.setBlock(pos, chest, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, this.lootTable);
        int count = this.spiresMin + random.nextInt(Math.max(1, this.spiresMax - this.spiresMin + 1));
        float start = random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < count; i++) {
            float angle = start + Mth.TWO_PI * i / count + (random.nextFloat() - 0.5F) * 0.5F;
            float distance = 1.5F + random.nextFloat() * 2.0F;
            int sx = x + Math.round(Mth.cos(angle) * distance);
            int sz = z + Math.round(Mth.sin(angle) * distance);
            if (sx == x && sz == z) {
                continue;
            }
            int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz) - 1;
            if (Math.abs(top + 1 - pos.getY()) > 1 || !DripstoneSpires.rock(level.getBlockState(new BlockPos(sx, top, sz)))) {
                continue;
            }
            int height = this.spireMinHeight + random.nextInt(Math.max(1, this.spireMaxHeight - this.spireMinHeight + 1));
            DripstoneSpires.spire(level, sx, top + 1, sz, height);
        }
        return true;
    }
}
