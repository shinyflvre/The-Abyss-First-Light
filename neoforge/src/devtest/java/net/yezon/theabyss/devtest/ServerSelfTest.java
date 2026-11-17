package net.yezon.theabyss.devtest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

public final class ServerSelfTest {
    static final String NS = "theabyss";
    private static final Logger LOG = TheabyssDevTest.LOGGER;
    private static final List<Entity> spawned = new ArrayList<>();
    private static int entityCheckCountdown = -1;
    private static final List<String> entityFailures = new ArrayList<>();

    private ServerSelfTest() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("abysstest")
            .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("patch").then(Commands.argument("feature", StringArgumentType.greedyString()).executes(ctx -> {
                BlockPos at = BlockPos.containing(ctx.getSource().getPosition());
                patch(ctx.getSource().getServer(), ctx.getSource().getLevel(), StringArgumentType.getString(ctx, "feature"), at);
                return 1;
            })))
            .then(Commands.argument("phase", StringArgumentType.word()).executes(ctx -> {
                run(ctx.getSource().getServer(), StringArgumentType.getString(ctx, "phase"));
                return 1;
            })));
    }

    public static void run(MinecraftServer server, String phase) {
        switch (phase) {
            case "all" -> {
                registries(server);
                dims(server);
                blocks(server);
                data(server);
                entities(server);
            }
            case "registries" -> registries(server);
            case "dims" -> dims(server);
            case "blocks" -> blocks(server);
            case "data" -> data(server);
            case "entities" -> entities(server);
            case "features" -> WorldgenSelfTest.features(server);
            case "solid" -> WorldgenSelfTest.solid(server);
            case "blossom" -> BlossomSelfTest.run(server);
            default -> LOG.warn("[ABYSS-TEST] unknown phase {}", phase);
        }
    }

    private static boolean ours(ResourceLocation id) {
        return id != null && NS.equals(id.getNamespace());
    }

    static void registries(MinecraftServer server) {
        Map<String, Integer> counts = new TreeMap<>();
        counts.put("block", (int) BuiltInRegistries.BLOCK.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("item", (int) BuiltInRegistries.ITEM.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("entity_type", (int) BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("mob_effect", (int) BuiltInRegistries.MOB_EFFECT.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("sound_event", (int) BuiltInRegistries.SOUND_EVENT.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("particle_type", (int) BuiltInRegistries.PARTICLE_TYPE.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("block_entity_type", (int) BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("menu", (int) BuiltInRegistries.MENU.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("fluid", (int) BuiltInRegistries.FLUID.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("feature_type", (int) BuiltInRegistries.FEATURE.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("recipe_type", (int) BuiltInRegistries.RECIPE_TYPE.keySet().stream().filter(ServerSelfTest::ours).count());
        counts.put("creative_tab", (int) BuiltInRegistries.CREATIVE_MODE_TAB.keySet().stream().filter(ServerSelfTest::ours).count());
        LOG.info("[ABYSS-TEST] registry counts {}", counts);

        List<String> problems = new ArrayList<>();
        for (Map.Entry<net.minecraft.resources.ResourceKey<EntityType<?>>, EntityType<?>> e : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (!ours(e.getKey().location())) {
                continue;
            }
            EntityType<?> type = e.getValue();
            if (type.getBaseClass() != null && LivingEntity.class.isAssignableFrom(type.getBaseClass()) && !DefaultAttributes.hasSupplier(type)) {
                problems.add("no attributes: " + e.getKey().location());
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!ours(id)) {
                continue;
            }
            try {
                ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) {
                    problems.add("empty default stack: " + id);
                }
                stack.getHoverName();
            } catch (Throwable t) {
                problems.add("item " + id + ": " + t);
            }
        }
        report("registries", problems);
    }

    static void dims(MinecraftServer server) {
        List<String> problems = new ArrayList<>();
        int found = 0;
        for (ServerLevel level : server.getAllLevels()) {
            ResourceLocation id = level.dimension().location();
            if (!ours(id)) {
                continue;
            }
            found++;
            try {
                level.getChunk(0, 0, ChunkStatus.FULL, true);
                int h = level.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8);
                BlockPos top = new BlockPos(8, h - 1, 8);
                LOG.info("[ABYSS-TEST] dimension {} surface y={} top={} biome={}", id, h, BuiltInRegistries.BLOCK.getKey(level.getBlockState(top).getBlock()),
                    level.getBiome(top).unwrapKey().map(k -> k.location().toString()).orElse("?"));
            } catch (Throwable t) {
                problems.add("dimension " + id + ": " + t);
                LOG.error("[ABYSS-TEST] dimension {} failed", id, t);
            }
        }
        if (found != 3) {
            problems.add("expected 3 mod dimensions, found " + found);
        }
        report("dims", problems);
    }

    static void blocks(MinecraftServer server) {
        ServerLevel level = server.overworld();
        List<String> problems = new ArrayList<>();
        List<String> noDrops = new ArrayList<>();
        List<String> changed = new ArrayList<>();
        BlockPos base = new BlockPos(1000, 250, 1000);
        level.getChunk(base.getX() >> 4, base.getZ() >> 4, ChunkStatus.FULL, true);
        int i = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!ours(id)) {
                continue;
            }
            BlockPos pos = base.offset((i % 32) * 2, 0, (i / 32) * 2);
            i++;
            try {
                level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
                level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                BlockState state = block.defaultBlockState();
                level.setBlock(pos, state, Block.UPDATE_ALL);
                BlockState now = level.getBlockState(pos);
                if (now != state) {
                    changed.add(id + " -> " + BuiltInRegistries.BLOCK.getKey(now.getBlock()));
                }
                List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, new ItemStack(net.minecraft.world.item.Items.NETHERITE_PICKAXE));
                if (drops.isEmpty()) {
                    noDrops.add(id.getPath());
                }
            } catch (Throwable t) {
                problems.add("block " + id + ": " + t);
                LOG.error("[ABYSS-TEST] block {} failed", id, t);
            } finally {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        LOG.info("[ABYSS-TEST] blocks placed: {}", i);
        LOG.info("[ABYSS-TEST] blocks whose state changed right after placement (support/updates, review): {}", changed);
        LOG.info("[ABYSS-TEST] blocks without drops for a netherite pickaxe (review against 1.20.1): {}", noDrops);
        report("blocks", problems);
    }

    static void data(MinecraftServer server) {
        long recipes = server.getRecipeManager().getRecipes().stream().filter(r -> ours(r.id())).count();
        long advancements = server.getAdvancements().getAllAdvancements().stream().filter(a -> ours(a.id())).count();
        long lootTables = server.reloadableRegistries().getKeys(Registries.LOOT_TABLE).stream()
            .filter(ServerSelfTest::ours).count();
        LOG.info("[ABYSS-TEST] data: recipes={} advancements={} loot_tables={}", recipes, advancements, lootTables);
        List<String> problems = new ArrayList<>();
        if (recipes == 0) {
            problems.add("no recipes loaded");
        }
        if (advancements == 0) {
            problems.add("no advancements loaded");
        }
        report("data", problems);
    }

    static void entities(MinecraftServer server) {
        ServerLevel level = server.getAllLevels().iterator().next();
        for (ServerLevel l : server.getAllLevels()) {
            if (l.dimension().location().toString().equals("theabyss:the_abyss")) {
                level = l;
            }
        }
        BlockPos base = new BlockPos(2000, 200, 2000);
        spawned.clear();
        entityFailures.clear();
        int i = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!ours(id)) {
                continue;
            }
            BlockPos pos = base.offset((i % 8) * 6, 0, (i / 8) * 6);
            i++;
            try {
                level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
                level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        level.setBlock(pos.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
                Entity e = type.spawn(level, pos, MobSpawnType.COMMAND);
                if (e == null) {
                    entityFailures.add("spawn returned null: " + id);
                    continue;
                }
                spawned.add(e);
                if (e instanceof LivingEntity living) {
                    living.hurt(level.damageSources().generic(), 1.0F);
                }
            } catch (Throwable t) {
                entityFailures.add("entity " + id + ": " + t);
                LOG.error("[ABYSS-TEST] entity {} failed", id, t);
            }
        }
        LOG.info("[ABYSS-TEST] entities spawned: {} of {}", spawned.size(), i);
        entityCheckCountdown = 200;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (entityCheckCountdown < 0) {
            return;
        }
        if (entityCheckCountdown-- > 0) {
            return;
        }
        List<String> gone = new ArrayList<>();
        for (Entity e : spawned) {
            if (e.isRemoved()) {
                gone.add(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath() + "(" + e.getRemovalReason() + ")");
            }
        }
        LOG.info("[ABYSS-TEST] entities removed within 200 ticks (projectiles/despawn are expected, review others): {}", gone);
        report("entities", entityFailures);
        spawned.forEach(e -> {
            if (!e.isRemoved()) {
                e.discard();
            }
        });
        spawned.clear();
    }

    static void patch(MinecraftServer server, ServerLevel level, String featureId, BlockPos at) {
        var reg = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        var placed = reg.get(ResourceLocation.parse(featureId));
        if (placed == null) {
            LOG.error("[ABYSS-TEST] patch: unknown placed feature {}", featureId);
            return;
        }
        BlockPos origin = new BlockPos(at.getX() & ~15, level.getMinBuildHeight(), at.getZ() & ~15);
        level.getChunk(origin.getX() >> 4, origin.getZ() >> 4, ChunkStatus.FULL, true);
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX() + 8, origin.getZ() + 8);
        var biome = level.getBiome(new BlockPos(origin.getX() + 8, top, origin.getZ() + 8));
        boolean inBiome = biome.value().getGenerationSettings().hasFeature(placed);
        java.util.function.IntSupplier count = () -> {
            int n = 0;
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-8, 0, -8).atY(level.getMinBuildHeight()), origin.offset(23, 0, 23).atY(level.getMaxBuildHeight() - 1))) {
                if (!level.getBlockState(p).isAir() && BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).getPath().contains("grass")) {
                    n++;
                }
            }
            return n;
        };
        int before = count.getAsInt();
        int placedRuns = 0;
        for (int i = 0; i < 10; i++) {
            if (placed.placeWithBiomeCheck(level, level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(i), origin)) {
                placedRuns++;
            }
        }
        int after = count.getAsInt();
        LOG.info("[ABYSS-TEST] patch {} at chunk {} top={} biome={} featureInBiome={} runsPlacedSomething={}/10 grassBlocks {} -> {}",
            featureId, origin, top, biome.unwrapKey().map(k -> k.location().toString()).orElse("?"), inBiome, placedRuns, before, after);
    }

    private static void report(String phase, List<String> problems) {
        if (problems.isEmpty()) {
            LOG.info("[ABYSS-TEST] RESULT {} PASS", phase);
        } else {
            LOG.error("[ABYSS-TEST] RESULT {} FAIL ({}): {}", phase, problems.size(), problems);
        }
    }
}
