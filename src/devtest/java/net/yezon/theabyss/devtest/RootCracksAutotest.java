package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.client.fx.RootCracks;
import net.yezon.theabyss.configuration.ConfigConfiguration;

final class RootCracksAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static BlockPos grove;
    private static int groundY;
    private static long startNanos;
    private static Object watched;
    private static int waited;
    private static int editX;
    private static int editZ;

    private RootCracksAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("rc setup", () -> {
            command.accept("gamemode spectator @s");
            command.accept("gamerule spawn_mobs false");
            ConfigConfiguration.ROOT_CRACKS.set(true);
        }, 4);
        add.add("rc find", () -> onServer(sp -> {
            ServerLevel abyss = sp.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:the_abyss")));
            var found = abyss.findClosestBiome3d(h -> h.is(ResourceKey.create(Registries.BIOME, Identifier.parse("theabyss:nightgrove"))),
                new BlockPos(0, 100, 0), 3000, 32, 64);
            grove = found == null ? null : found.getFirst();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] root cracks nightgrove at {}", grove);
        }), 10);
        add.add("rc go", () -> {
            if (grove != null) {
                command.accept("execute in theabyss:the_abyss run forceload add " + (grove.getX() - 48) + " " + (grove.getZ() - 48) + " " + (grove.getX() + 48) + " "
                    + (grove.getZ() + 48));
                command.accept("execute in theabyss:the_abyss run tp @s " + grove.getX() + " 200 " + grove.getZ());
                startNanos = System.nanoTime();
            }
        }, 200);
        add.add("rc ground", () -> onServer(sp -> {
            groundY = sp.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sp.getBlockX(), sp.getBlockZ());
            command.accept("execute in theabyss:the_abyss run tp @s " + (sp.getBlockX() + 0.5) + " " + (groundY + 2.6) + " " + (sp.getBlockZ() + 0.5) + " 0 38");
            int[][] spots = {{0, 4}, {2, 6}, {-2, 5}};
            String[] mobs = {"minecraft:zombie", "minecraft:iron_golem", "minecraft:cow"};
            for (int i = 0; i < spots.length; i++) {
                int x = sp.getBlockX() + spots[i][0];
                int z = sp.getBlockZ() + spots[i][1];
                int y = sp.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                command.accept("execute in theabyss:the_abyss run summon " + mobs[i] + " " + (x + 0.5) + " " + y + " " + (z + 0.5) + " {NoAI:1b,PersistenceRequired:1b}");
            }
        }), 160);
        add.add("rc stats", () -> {
            int[] s = stats();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] root cracks after {} s: chunks {} with cracks {} quads rim/halo/core {}/{}/{}",
                (System.nanoTime() - startNanos) / 1.0E9, s[0], s[1], s[2], s[3], s[4]);
            check("chunks were built (" + s[0] + ")", s[0] > 50);
            check("cracks found in the nightgrove (" + s[1] + " chunks)", s[1] > 5);
        }, 2);
        for (int round = 0; round < 2; round++) {
            boolean place = round == 0;
            add.add("rc edit " + round, () -> {
                Minecraft mc = Minecraft.getInstance();
                editX = mc.player.getBlockX() + 3;
                editZ = mc.player.getBlockZ() + 1;
                watched = chunkAt(editX, editZ);
                waited = 0;
                command.accept("execute in theabyss:the_abyss run setblock " + editX + " " + (place ? groundY : groundY) + " " + editZ + " "
                    + (place ? "theabyss:nightgrove_dirt" : "minecraft:air"));
            }, 1);
            for (int t = 0; t < 60; t++) {
                add.add("rc wait " + round + " " + t, () -> {
                    if (waited >= 0 && chunkAt(editX, editZ) != watched) {
                        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] root cracks rebuilt {} ticks after the block {}", waited, place ? "was placed" : "was removed");
                        waited = -1000;
                    } else if (waited >= 0) {
                        waited++;
                    }
                }, 1);
            }
            add.add("rc edit check " + round, () -> check("cracks follow the " + (place ? "placed" : "removed") + " block within 20 ticks (" + waited + ")",
                waited < 0 || waited <= 20), 2);
        }
        int[] yaws = {0, 90, 270};
        for (int yaw : yaws) {
            add.add("rc look " + yaw, () -> command.accept("tp @s ~ " + (groundY + 2.6) + " ~ " + yaw + " 38"), 20);
            add.add("rc shot " + yaw, () -> shot.accept("122_root_cracks_moody_" + yaw), 6);
        }
        add.add("rc up 10", () -> command.accept("tp @s ~ " + (groundY + 12) + " ~ 0 55"), 30);
        add.add("rc shot 10", () -> shot.accept("122_root_cracks_high_10"), 6);
        add.add("rc up 30", () -> command.accept("tp @s ~ " + (groundY + 32) + " ~ 0 60"), 30);
        add.add("rc shot 30", () -> shot.accept("122_root_cracks_high_30"), 6);
        add.add("rc find crack", () -> {
            float[] best = nearestCrack();
            if (best != null) {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] root cracks nearest crack at {} {} {}", best[0], best[1], best[2]);
                command.accept("execute in theabyss:the_abyss run tp @s " + best[0] + " " + (best[1] + 3.0) + " " + (best[2] - 4.5) + " 0 40");
            }
        }, 40);
        add.add("rc shot crack", () -> shot.accept("122_root_cracks_group_close"), 6);
        add.add("rc crack up", () -> command.accept("tp @s ~ ~9 ~-4 0 55"), 30);
        add.add("rc shot crack up", () -> shot.accept("122_root_cracks_group_high"), 6);
        add.add("rc crack bright", () -> ConfigConfiguration.ABYSS_BRIGHTNESS.set(100), 30);
        add.add("rc shot crack bright", () -> shot.accept("122_root_cracks_group_bright"), 6);
        add.add("rc bright", () -> ConfigConfiguration.ABYSS_BRIGHTNESS.set(100), 30);
        add.add("rc shot bright 30", () -> shot.accept("122_root_cracks_bright_30"), 6);
        add.add("rc bright low", () -> command.accept("tp @s ~ " + (groundY + 2.6) + " ~ 0 38"), 30);
        add.add("rc shot bright low", () -> shot.accept("122_root_cracks_bright_0"), 6);
        add.add("rc off", () -> ConfigConfiguration.ROOT_CRACKS.set(false), 10);
        add.add("rc off check", () -> check("config off removes every crack (" + stats()[0] + " chunks)", stats()[0] == 0), 2);
        add.add("rc shot off", () -> shot.accept("122_root_cracks_off"), 6);
        add.add("rc on", () -> ConfigConfiguration.ROOT_CRACKS.set(true), 160);
        add.add("rc on check", () -> check("config on builds them again (" + stats()[1] + " chunks with cracks)", stats()[1] > 0), 2);
        add.add("rc reset", () -> ConfigConfiguration.ABYSS_BRIGHTNESS.set(0), 4);
        add.add("rc result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT root_cracks {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static float[] nearestCrack() {
        Minecraft mc = Minecraft.getInstance();
        float[] best = null;
        double bestDistance = Double.MAX_VALUE;
        try {
            var field = RootCracks.class.getDeclaredField("CHUNKS");
            field.setAccessible(true);
            Map<?, ?> chunks = (Map<?, ?>)field.get(null);
            for (Object chunk : chunks.values()) {
                var sparksField = chunk.getClass().getDeclaredField("sparks");
                sparksField.setAccessible(true);
                float[] sparks = (float[])sparksField.get(chunk);
                for (int i = 0; i + 2 < sparks.length; i += 7) {
                    double d = mc.player.position().distanceToSqr(sparks[i], sparks[i + 1], sparks[i + 2]);
                    net.minecraft.core.BlockPos camera = net.minecraft.core.BlockPos.containing(sparks[i], sparks[i + 1] + 3.0, sparks[i + 2] - 4.5);
                    if (d < bestDistance && mc.level.canSeeSky(camera) && mc.level.canSeeSky(net.minecraft.core.BlockPos.containing(sparks[i], sparks[i + 1] + 1.0, sparks[i + 2]))) {
                        bestDistance = d;
                        best = new float[] {sparks[i], sparks[i + 1], sparks[i + 2]};
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            TheabyssDevTest.LOGGER.warn("[ABYSS-AUTOTEST] root cracks nearest failed", e);
        }
        return best;
    }

    private static Object chunkAt(int x, int z) {
        try {
            var field = RootCracks.class.getDeclaredField("CHUNKS");
            field.setAccessible(true);
            Map<?, ?> chunks = (Map<?, ?>)field.get(null);
            return chunks.get(net.minecraft.world.level.ChunkPos.pack(x >> 4, z >> 4));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static int[] stats() {
        int[] out = new int[5];
        try {
            var field = RootCracks.class.getDeclaredField("CHUNKS");
            field.setAccessible(true);
            Map<?, ?> chunks = (Map<?, ?>)field.get(null);
            out[0] = chunks.size();
            for (Object chunk : chunks.values()) {
                var quadsField = chunk.getClass().getDeclaredField("quads");
                quadsField.setAccessible(true);
                int[] quads = (int[])quadsField.get(chunk);
                if (quads[0] + quads[1] + quads[2] > 0) {
                    out[1]++;
                }
                out[2] += quads[0];
                out[3] += quads[1];
                out[4] += quads[2];
            }
        } catch (ReflectiveOperationException e) {
            TheabyssDevTest.LOGGER.warn("[ABYSS-AUTOTEST] root cracks stats failed", e);
        }
        return out;
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] root cracks check {}: {}", name, ok ? "ok" : "FAILED");
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
