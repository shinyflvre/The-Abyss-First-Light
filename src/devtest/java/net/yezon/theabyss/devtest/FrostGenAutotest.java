package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

final class FrostGenAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final int X = 9000;
    private static final int Z = 9000;
    private static BlockPos spike;
    private static boolean clearView;

    private FrostGenAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
            (net.neoforged.neoforge.client.event.ViewportEvent.RenderFog event) -> {
                if (clearView) {
                    event.setNearPlaneDistance(400.0F);
                    event.setFarPlaneDistance(500.0F);
                }
            });
        add.add("fg go", () -> {
            command.accept("gamemode spectator @s");
            command.accept("gamerule spawn_mobs false");
            command.accept("effect give @s minecraft:night_vision 9999 0 true");
            command.accept("execute in theabyss:frost_world run forceload add " + (X - 96) + " " + (Z - 96) + " " + (X + 96) + " " + (Z + 96));
            command.accept("execute in theabyss:frost_world run tp @s " + X + " 260 " + Z + " 45 50");
        }, 900);
        add.add("fg count", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.level();
            Map<String, Integer> counts = new TreeMap<>();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int floating = 0;
            int trunks = 0;
            for (int x = X - 96; x < X + 96; x++) {
                for (int z = Z - 96; z < Z + 96; z++) {
                    int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    for (int y = top; y > top - 70 && y > level.getMinY(); y--) {
                        var state = level.getBlockState(p.set(x, y, z));
                        if (state.is(Blocks.BLUE_ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.ICE)) {
                            counts.merge(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath(), 1, Integer::sum);
                            if (state.is(Blocks.BLUE_ICE) && (spike == null || y > spike.getY())) {
                                spike = p.immutable();
                            }
                        }
                        if (state.is(net.minecraft.tags.BlockTags.LOGS) && !level.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.LOGS)
                            && level.getBlockState(p.above()).is(net.minecraft.tags.BlockTags.LOGS) && level.getBlockState(p.above(2)).is(net.minecraft.tags.BlockTags.LOGS)
                            && level.getBlockState(p.above(3)).is(net.minecraft.tags.BlockTags.LOGS)) {
                            if (level.getBlockState(p.below()).isAir()) {
                                floating++;
                            } else {
                                trunks++;
                            }
                        }
                    }
                }
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frost gen blocks {} standing trunk columns {} floating trunk columns {} highest blue ice {}", counts, trunks, floating, spike);
            check("ice spikes generated (" + counts + ")", counts.getOrDefault("blue_ice", 0) > 50);
            check("no powder snow", counts.getOrDefault("powder_snow", 0) == 0);
        }), 20);
        add.add("fg clear", () -> {
            clearView = true;
            command.accept("execute in theabyss:frost_world run tp @s " + (X - 70) + " 190 " + (Z - 70) + " -45 30");
        }, 40);
        add.add("fg shot high", () -> shot.accept("126_frostgen_high"), 6);
        add.add("fg look 2", () -> command.accept("execute in theabyss:frost_world run tp @s " + (X + 70) + " 170 " + (Z + 70) + " 135 25"), 40);
        add.add("fg shot high 2", () -> shot.accept("126_frostgen_high_2"), 6);
        add.add("fg look 3", () -> command.accept("execute in theabyss:frost_world run tp @s " + (X + 70) + " 150 " + (Z - 70) + " 45 15"), 40);
        add.add("fg shot high 3", () -> shot.accept("126_frostgen_high_3"), 6);
        add.add("fg look 4", () -> command.accept("execute in theabyss:frost_world run tp @s " + X + " 230 " + Z + " 0 89"), 40);
        add.add("fg shot top", () -> shot.accept("126_frostgen_top"), 6);
        add.add("fg storm", () -> clearView = false, 4);
        add.add("fg spike", () -> {
            if (spike != null) {
                command.accept("execute in theabyss:frost_world run tp @s " + (spike.getX() + 22) + " " + (spike.getY() - 8) + " " + (spike.getZ() + 22) + " 135 5");
            }
        }, 60);
        add.add("fg shot spike", () -> shot.accept("126_frostgen_spike"), 6);
        add.add("fg ground", () -> onServer(sp -> {
            int y = sp.level().getHeight(Heightmap.Types.MOTION_BLOCKING, X + 30, Z + 30);
            command.accept("execute in theabyss:frost_world run tp @s " + (X + 30) + " " + (y + 3) + " " + (Z + 30) + " 200 0");
        }), 60);
        add.add("fg shot ground", () -> shot.accept("126_frostgen_ground"), 6);
        add.add("fg result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT frostgen {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frostgen check {}: {}", name, ok ? "ok" : "FAILED");
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
