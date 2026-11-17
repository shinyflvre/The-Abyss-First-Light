package net.yezon.theabyss.devtest;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class LushCaveAutotest {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static int CX = 640;
    private static int CZ = -240;
    private static BlockPos view = new BlockPos(CX, 16, CZ);

    private LushCaveAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("lush find", () -> onServer(sp -> {
            ServerLevel level = sp.serverLevel().getServer().getLevel(ABYSS);
            var found = level.findClosestBiome3d(h -> h.is(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("theabyss:lush_glow_cave"))),
                new BlockPos(-6000, 0, -6000), 3000, 32, 64);
            if (found != null) {
                CX = found.getFirst().getX();
                CZ = found.getFirst().getZ();
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] lush fresh area at {} {}", CX, CZ);
        }), 10);
        add.add("lush go", () -> {
            command.accept("gamemode spectator @s");
            command.accept("execute in theabyss:the_abyss run forceload add " + (CX - 48) + " " + (CZ - 48) + " " + (CX + 48) + " " + (CZ + 48));
            command.accept("execute in theabyss:the_abyss run tp @s " + CX + " 40 " + CZ);
        }, 400);
        add.add("lush scan", () -> onServer(sp -> {
            ServerLevel level = sp.serverLevel().getServer().getLevel(ABYSS);
            Map<String, Integer> counts = new TreeMap<>();
            BlockPos best = null;
            int bestScore = -1;
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int x = CX - 40; x <= CX + 40; x++) {
                for (int z = CZ - 40; z <= CZ + 40; z++) {
                    for (int y = -50; y <= 60; y++) {
                        BlockState s = level.getBlockState(p.set(x, y, z));
                        String id = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
                        if (id.startsWith("lush_")) {
                            counts.merge(id, 1, Integer::sum);
                        }
                        String ns = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace();
                        if (ns.equals("minecraft") && !s.isAir() && !s.is(net.minecraft.world.level.block.Blocks.WATER)
                            && !s.is(net.minecraft.world.level.block.Blocks.BEDROCK) && !s.is(net.minecraft.world.level.block.Blocks.LAVA)
                            && level.getBiome(p).is(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("theabyss:lush_glow_cave")))) {
                            counts.merge("VANILLA " + id, 1, Integer::sum);
                        }
                    }
                }
            }
            for (int x = CX - 36; x <= CX + 36; x += 4) {
                for (int z = CZ - 36; z <= CZ + 36; z += 4) {
                    for (int y = -46; y <= 56; y += 2) {
                        if (!level.getBlockState(p.set(x, y, z)).isAir() || !level.getBlockState(p.set(x, y + 1, z)).isAir()) {
                            continue;
                        }
                        int score = 0;
                        int air = 0;
                        for (int dx = -6; dx <= 6; dx += 2) {
                            for (int dz = -6; dz <= 6; dz += 2) {
                                for (int dy = -4; dy <= 4; dy += 2) {
                                    BlockState s = level.getBlockState(p.set(x + dx, y + dy, z + dz));
                                    if (s.isAir()) {
                                        air++;
                                    } else if (BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath().startsWith("lush_")) {
                                        score++;
                                    }
                                }
                            }
                        }
                        score = air > 120 ? score : 0;
                        if (score > bestScore) {
                            bestScore = score;
                            best = new BlockPos(x, y, z);
                        }
                    }
                }
            }
            if (best != null) {
                view = best;
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] lush blocks in 81x111x81: {}", counts);
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] lush view at {} (score {})", view, bestScore);
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT lush {} ({} lush blocks)", counts.values().stream().mapToInt(Integer::intValue).sum() > 500 ? "PASS" : "FAIL",
                counts.values().stream().mapToInt(Integer::intValue).sum());
        }), 10);
        String[] dirs = {"0", "90", "180", "270"};
        for (int i = 0; i < dirs.length; i++) {
            String yaw = dirs[i];
            int n = i;
            add.add("lush view " + yaw, () -> command.accept("execute in theabyss:the_abyss run tp @s " + (view.getX() + 0.5) + " " + view.getY() + " " + (view.getZ() + 0.5) + " " + yaw + " 10"), 60);
            add.add("lush shot " + yaw, () -> shot.accept("112_lush_" + n + "_dark"), 2);
        }
        add.add("lush night vision", () -> command.accept("effect give @s minecraft:night_vision 120 0 true"), 20);
        for (int i = 0; i < dirs.length; i++) {
            String yaw = dirs[i];
            int n = i;
            add.add("lush nv view " + yaw, () -> command.accept("execute in theabyss:the_abyss run tp @s " + (view.getX() + 0.5) + " " + view.getY() + " " + (view.getZ() + 0.5) + " " + yaw + " 10"), 30);
            add.add("lush nv shot " + yaw, () -> shot.accept("112_lush_" + n + "_lit"), 2);
        }
        add.add("lush back", () -> {
            command.accept("effect clear @s");
            command.accept("gamemode creative @s");
        }, 4);
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
