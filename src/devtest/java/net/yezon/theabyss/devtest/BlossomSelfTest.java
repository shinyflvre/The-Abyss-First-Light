package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.yezon.theabyss.block.BlossomRankBlock;
import net.yezon.theabyss.init.TheabyssModBlocks;
import org.slf4j.Logger;

final class BlossomSelfTest {
    private static final Logger LOG = TheabyssDevTest.LOGGER;
    private static final int RADIUS = 4;

    private BlossomSelfTest() {
    }

    static void run(MinecraftServer server) {
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("theabyss", "the_abyss")));
        ResourceKey<net.minecraft.world.level.biome.Biome> nightgrove = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("theabyss", "nightgrove"));
        int loran = 0;
        List<BlockPos> plants = new ArrayList<>();
        Map<Integer, Integer> heights = new TreeMap<>();
        int nightgroveColumns = 0;
        int areas = 0;
        for (BlockPos start : new BlockPos[] {new BlockPos(0, 64, 0), new BlockPos(3000, 64, 0), new BlockPos(0, 64, 3000)}) {
            var found = level.findClosestBiome3d(b -> b.is(nightgrove), start, 6400, 32, 64);
            if (found == null) {
                continue;
            }
            areas++;
            ChunkPos center = ChunkPos.containing(found.getFirst());
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    level.getChunk(center.x() + dx, center.z() + dz, ChunkStatus.FULL, true);
                }
            }
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int dx = -RADIUS + 1; dx < RADIUS; dx++) {
                for (int dz = -RADIUS + 1; dz < RADIUS; dz++) {
                    LevelChunk chunk = level.getChunk(center.x() + dx, center.z() + dz);
                    for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++) {
                        for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
                            if (level.getBiome(pos.set(x, 100, z)).is(nightgrove)) {
                                nightgroveColumns++;
                            }
                            for (int y = 40; y < 200; y++) {
                                BlockState state = chunk.getBlockState(pos.set(x, y, z));
                                if (state.is(TheabyssModBlocks.LORAN_FLOWER.get()) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                                    loran++;
                                } else if (state.is(TheabyssModBlocks.BLOSSOM_RANK.get()) && state.getValue(BlossomRankBlock.SEGMENT) == BlossomRankBlock.Segment.END) {
                                    int h = 1;
                                    while (level.getBlockState(pos.set(x, y - h, z)).is(TheabyssModBlocks.BLOSSOM_RANK.get())) {
                                        h++;
                                    }
                                    heights.merge(h, 1, Integer::sum);
                                    plants.add(new BlockPos(x, y, z));
                                    pos.set(x, y, z);
                                }
                            }
                        }
                    }
                }
            }
        }
        int grouped = 0;
        for (BlockPos p : plants) {
            for (BlockPos q : plants) {
                if (p != q && Math.abs(p.getX() - q.getX()) <= 4 && Math.abs(p.getZ() - q.getZ()) <= 4) {
                    grouped++;
                    break;
                }
            }
        }
        LOG.info("[ABYSS-TEST] blossom areas {} nightgrove columns {} loran {} blossom ranks {} (ratio {}) grouped {} single {} heights {}", areas,
            nightgroveColumns, loran, plants.size(), loran == 0 ? "-" : String.format("%.2f", plants.size() / (double)loran), grouped,
            plants.size() - grouped, heights);
        boolean ok = !plants.isEmpty() && heights.keySet().stream().allMatch(h -> h >= 3 && h <= 12);
        LOG.info("[ABYSS-TEST] RESULT blossom {}", ok ? "PASS" : "FAIL");
    }
}
