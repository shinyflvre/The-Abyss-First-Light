package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

final class WorldgenSelfTest {
    private static final Logger LOG = TheabyssDevTest.LOGGER;
    private static final int TRIES = 24;

    private WorldgenSelfTest() {
    }

    private static boolean ours(ResourceLocation id) {
        return id != null && ServerSelfTest.NS.equals(id.getNamespace());
    }

    static void features(MinecraftServer server) {
        List<String> problems = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if (!ours(level.dimension().location())) {
                continue;
            }
            var generator = level.getChunkSource().getGenerator();
            for (Holder<Biome> biome : generator.getBiomeSource().possibleBiomes()) {
                ResourceLocation biomeId = biome.unwrapKey().map(k -> k.location()).orElse(null);
                if (!ours(biomeId)) {
                    continue;
                }
                var found = level.findClosestBiome3d(b -> b.is(biome.unwrapKey().get()), new BlockPos(0, 64, 0), 6400, 32, 64);
                if (found == null) {
                    LOG.warn("[ABYSS-TEST] features {} in {}: biome not found within 6400 blocks", biomeId, level.dimension().location());
                    continue;
                }
                ChunkPos center = new ChunkPos(found.getFirst());
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        level.getChunk(center.x + dx, center.z + dz, ChunkStatus.FULL, true);
                    }
                }
                LOG.info("[ABYSS-TEST] features {} at {} natural: {}", biomeId, found.getFirst().toShortString(), naturalStats(level, center));

                List<String> never = new ArrayList<>();
                List<String> rates = new ArrayList<>();
                for (HolderSet<PlacedFeature> step : biome.value().getGenerationSettings().features()) {
                    for (Holder<PlacedFeature> feature : step) {
                        ResourceLocation featureId = feature.unwrapKey().map(k -> k.location()).orElse(null);
                        if (!ours(featureId)) {
                            continue;
                        }
                        int ok = 0;
                        for (int i = 0; i < TRIES; i++) {
                            int cx = center.x + i % 3 - 1;
                            int cz = center.z + i / 3 % 3 - 1;
                            try {
                                if (feature.value().placeWithBiomeCheck(level, generator, RandomSource.create(i * 31L + featureId.hashCode()),
                                    new BlockPos(cx * 16, level.getMinBuildHeight(), cz * 16))) {
                                    ok++;
                                }
                            } catch (Throwable t) {
                                LOG.error("[ABYSS-TEST] feature {} threw", featureId, t);
                                problems.add(featureId.getPath() + " threw " + t);
                                break;
                            }
                        }
                        rates.add(featureId.getPath() + "=" + ok);
                        if (ok == 0) {
                            never.add(featureId.getPath());
                        }
                    }
                }
                LOG.info("[ABYSS-TEST] features {} placed x/{}: {}", biomeId, TRIES, rates);
                if (!never.isEmpty()) {
                    LOG.warn("[ABYSS-TEST] features {} NEVER placed: {}", biomeId, never);
                    problems.add(biomeId.getPath() + " never: " + never);
                }
            }
        }

        var structures = server.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (var structure : structures.listElements().toList()) {
            ResourceLocation id = structure.key().location();
            if (!ours(id)) {
                continue;
            }
            String where = "-";
            for (ServerLevel level : server.getAllLevels()) {
                if (!ours(level.dimension().location()) && level != server.overworld()) {
                    continue;
                }
                var hit = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), new BlockPos(0, 64, 0), 64, false);
                if (hit != null) {
                    where = level.dimension().location().getPath() + " " + hit.getFirst().toShortString();
                    break;
                }
            }
            LOG.info("[ABYSS-TEST] structure {} nearest: {}", id, where);
            if (where.equals("-")) {
                problems.add("structure " + id.getPath() + " not found");
            }
        }
        if (problems.isEmpty()) {
            LOG.info("[ABYSS-TEST] RESULT features PASS");
        } else {
            LOG.error("[ABYSS-TEST] RESULT features FAIL ({}): {}", problems.size(), problems);
        }
    }

    static void solid(MinecraftServer server) {
        List<String> missing = new ArrayList<>();
        List<String> leaves = new ArrayList<>();
        List<String> washed = new ArrayList<>();
        for (var block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!ours(id)) {
                continue;
            }
            BlockState state = block.defaultBlockState();
            if (block instanceof net.minecraft.world.level.block.LeavesBlock) {
                if (!state.is(BlockTags.LEAVES)) {
                    leaves.add('"' + id.toString() + '"');
                }
            } else if (state.isSolid() && !state.blocksMotion() && !state.is(BlockTags.SIGNS)) {
                missing.add('"' + id.toString() + '"');
            } else if (!state.isSolid() && state.blocksMotion() && washedAwayIn1201(block, id)) {
                washed.add('"' + id.toString() + '"');
            }
        }
        LOG.info("[ABYSS-TEST] solid blocks that do not block motion ({}): [{}]", missing.size(), String.join(", ", missing));
        LOG.info("[ABYSS-TEST] non-solid blocks that fluids do not wash away ({}): [{}]", washed.size(), String.join(", ", washed));
        LOG.info("[ABYSS-TEST] leaves missing from #minecraft:leaves ({}): [{}]", leaves.size(), String.join(", ", leaves));
        LOG.info("[ABYSS-TEST] RESULT solid {}", missing.isEmpty() && leaves.isEmpty() && washed.isEmpty() ? "PASS" : "FAIL");
    }

    private static boolean washedAwayIn1201(net.minecraft.world.level.block.Block block, ResourceLocation id) {
        return !(block instanceof net.minecraft.world.level.block.LiquidBlock)
            && !(block instanceof net.minecraft.world.level.block.LiquidBlockContainer)
            && !(block instanceof net.minecraft.world.level.block.DoorBlock)
            && !(block instanceof net.minecraft.world.level.block.SignBlock)
            && !(block instanceof net.minecraft.world.level.block.LadderBlock)
            && !block.defaultBlockState().isAir()
            && !id.getPath().contains("portal");
    }

    private static String naturalStats(ServerLevel level, ChunkPos center) {
        int free = 0;
        int planted = 0;
        int trunks = 0;
        Map<String, Integer> ground = new TreeMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = (center.x - 1) * 16; x < (center.x + 2) * 16; x++) {
            for (int z = (center.z - 1) * 16; z < (center.z + 2) * 16; z++) {
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockState groundState = level.getBlockState(pos.set(x, top, z));
                if (groundState.is(BlockTags.LOGS)) {
                    trunks++;
                    continue;
                }
                ground.merge(BuiltInRegistries.BLOCK.getKey(groundState.getBlock()).getPath(), 1, Integer::sum);
                BlockState above = level.getBlockState(pos.set(x, top + 1, z));
                if (above.isAir()) {
                    free++;
                } else if (above.getFluidState().isEmpty() && !above.is(BlockTags.LEAVES)) {
                    free++;
                    planted++;
                }
            }
        }
        List<Map.Entry<String, Integer>> topGround = new ArrayList<>(ground.entrySet());
        topGround.sort((a, b) -> b.getValue() - a.getValue());
        return String.format("plants on free ground %d%% (%d/%d), trunk columns %d, ground %s", free == 0 ? 0 : planted * 100 / free, planted, free,
            trunks, topGround.subList(0, Math.min(4, topGround.size())));
    }
}
