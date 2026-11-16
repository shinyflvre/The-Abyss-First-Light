package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

final class SpawnAuditAutotest {
    private static final String[] DIMENSIONS = {"theabyss:the_abyss", "theabyss:frost_world"};
    private static final List<String> lines = new ArrayList<>();
    private static final List<ResourceKey<Biome>> biomes = new ArrayList<>();
    private static final Map<ResourceKey<Biome>, String> dimensionOf = new LinkedHashMap<>();
    private static BlockPos target;
    static String only = "";

    private SpawnAuditAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("sa setup", () -> {
            command.accept("gamemode creative @s");
            command.accept("difficulty normal");
            onServer(sp -> {
                for (String dim : DIMENSIONS) {
                    ServerLevel level = sp.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dim)));
                    if (level == null) {
                        continue;
                    }
                    for (Holder<Biome> b : level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
                        b.unwrapKey().ifPresent(k -> {
                            if (!dimensionOf.containsKey(k) && (only.isEmpty() || k.identifier().getPath().equals(only))) {
                                biomes.add(k);
                                dimensionOf.put(k, dim);
                            }
                        });
                    }
                }
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] spawn audit biomes {}", biomes);
            });
        }, 10);
        for (int i = 0; i < 24; i++) {
            int index = i;
            add.add("sa find " + i, () -> onServer(sp -> {
                target = null;
                if (index >= biomes.size()) {
                    return;
                }
                ResourceKey<Biome> key = biomes.get(index);
                ServerLevel level = sp.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimensionOf.get(key))));
                var found = level.findClosestBiome3d(h -> h.is(key), new BlockPos(0, 64, 0), 4000, 32, 32);
                target = found == null ? null : found.getFirst();
            }), 6);
            add.add("sa go " + i, () -> {
                if (target != null && index < biomes.size()) {
                    String dim = dimensionOf.get(biomes.get(index));
                    command.accept("execute in " + dim + " run forceload add " + (target.getX() - 32) + " " + (target.getZ() - 32) + " " + (target.getX() + 32) + " "
                        + (target.getZ() + 32));
                    command.accept("execute in " + dim + " run tp @s " + target.getX() + " " + (target.getY() + 40) + " " + target.getZ());
                }
            }, 240);
            add.add("sa audit " + i, () -> onServer(sp -> {
                if (index >= biomes.size()) {
                    return;
                }
                ResourceKey<Biome> key = biomes.get(index);
                if (target == null) {
                    lines.add(key.identifier() + ": not found");
                    return;
                }
                ServerLevel level = sp.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimensionOf.get(key))));
                for (int cx = (target.getX() - 32) >> 4; cx <= (target.getX() + 32) >> 4; cx++) {
                    for (int cz = (target.getZ() - 32) >> 4; cz <= (target.getZ() + 32) >> 4; cz++) {
                        level.getChunk(cx, cz);
                    }
                }
                audit(level, key, target);
            }), 4);
        }
        add.add("sa report", () -> {
            for (String l : lines) {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] spawn audit {}", l);
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT spawn_audit DONE ({} lines)", lines.size());
        }, 2);
    }

    private static void audit(ServerLevel level, ResourceKey<Biome> key, BlockPos centre) {
        RandomSource random = RandomSource.create(42);
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centre.getX(), centre.getZ());
        lines.add(key.identifier() + " target " + centre + " surface " + surface + " biome at surface " + level.getBiome(centre.atY(surface)).unwrapKey().map(Object::toString).orElse("?")
            + " biome at target " + level.getBiome(centre).unwrapKey().map(Object::toString).orElse("?") + " loaded " + level.hasChunkAt(centre));
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        boolean cave = centre.getY() < level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centre.getX(), centre.getZ()) - 12;
        for (int dx = -24; dx <= 24; dx += 2) {
            for (int dz = -24; dz <= 24; dz += 2) {
                int x = centre.getX() + dx;
                int z = centre.getZ() + dz;
                if (cave) {
                    for (int y = centre.getY() + 16; y > centre.getY() - 16; y--) {
                        if (level.getBlockState(p.set(x, y, z)).isAir() && !level.getBlockState(p.set(x, y - 1, z)).isAir()) {
                            positions.add(new BlockPos(x, y, z));
                            break;
                        }
                    }
                } else {
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos top = new BlockPos(x, y, z);
                    positions.add(top);
                    if (!level.getFluidState(top.below()).isEmpty()) {
                        positions.add(top.below(2));
                    }
                }
            }
        }
        positions.removeIf(pos -> !level.getBiome(pos).is(key));
        Map<String, Integer> ground = new java.util.TreeMap<>();
        for (BlockPos pos : positions) {
            ground.merge(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.below()).getBlock()).getPath() + "/" + BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath(), 1, Integer::sum);
        }
        lines.add(key.identifier() + " ground/at: " + ground);
        Map<String, int[]> result = new LinkedHashMap<>();
        Map<String, String> info = new LinkedHashMap<>();
        for (MobCategory category : MobCategory.values()) {
            if (category == MobCategory.MISC) {
                continue;
            }
            for (BlockPos pos : positions) {
                for (var weighted : level.getChunkSource().getGenerator().getMobsAt(level, level.structureManager(), category, pos).unwrap()) {
                    MobSpawnSettings.SpawnerData data = weighted.value();
                    EntityType<?> type = data.type();
                    String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                    info.putIfAbsent(id, category + " w" + weighted.weight());
                    int[] r = result.computeIfAbsent(id, k -> new int[6]);
                    r[0]++;
                    if (!SpawnPlacements.isSpawnPositionOk(type, level, pos)) {
                        r[1]++;
                        continue;
                    }
                    if (!SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, random)) {
                        r[2]++;
                        continue;
                    }
                    if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
                        r[3]++;
                        continue;
                    }
                    Entity e = type.create(level, EntitySpawnReason.NATURAL);
                    if (!(e instanceof Mob mob)) {
                        r[4]++;
                        continue;
                    }
                    mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                    if (!net.neoforged.neoforge.event.EventHooks.checkSpawnPosition(mob, level, EntitySpawnReason.NATURAL)) {
                        r[4]++;
                        mob.discard();
                        continue;
                    }
                    mob.discard();
                    r[5]++;
                }
            }
        }
        StringBuilder b = new StringBuilder(key.identifier() + " (" + positions.size() + " positions" + (cave ? ", cave" : "") + "):");
        for (Map.Entry<String, int[]> e : result.entrySet()) {
            int[] r = e.getValue();
            b.append(String.format(java.util.Locale.ROOT, "%n    %-30s %-18s pass %3d%%  (placement %d, rules %d, room %d, mob check %d)", e.getKey(), info.get(e.getKey()),
                r[0] == 0 ? 0 : 100 * r[5] / r[0], r[1], r[2], r[3], r[4]));
        }
        if (result.isEmpty()) {
            b.append(" no spawns listed");
        }
        lines.add(b.toString());
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                action.accept(sp);
            }
        });
    }
}
