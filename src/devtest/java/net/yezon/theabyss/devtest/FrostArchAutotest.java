package net.yezon.theabyss.devtest;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.yezon.theabyss.world.structures.GiantArchPiece;
import net.yezon.theabyss.world.structures.GiantArchShape;

final class FrostArchAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static BlockPos found;
    private static GiantArchShape shape;
    private static boolean clearView;
    private static final List<GiantArchShape> shapes = new ArrayList<>();
    private static final int[][] ORIGINS = {{0, 0}, {1600, 0}, {0, 1600}};
    private static String dimension = "theabyss:frost_world";
    private static String[] targets = {"frost_arch", "frost_arch", "frost_arch"};

    static void abyss() {
        dimension = "theabyss:the_abyss";
        targets = new String[] {"blue_mountain_arch", "blue_mountain_arch", "phantom_crate_arch"};
    }

    private FrostArchAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
            (net.neoforged.neoforge.client.event.ViewportEvent.RenderFog event) -> {
                if (clearView) {
                    event.setNearPlaneDistance(500.0F);
                    event.setFarPlaneDistance(600.0F);
                }
            });
        add.add("fa go", () -> {
            command.accept("gamemode spectator @s");
            command.accept("gamerule spawn_mobs false");
            command.accept("effect give @s minecraft:night_vision 9999 0 true");
            command.accept("execute in " + dimension + " run tp @s 0 250 0");
        }, 200);
        for (int n = 0; n < ORIGINS.length; n++) {
            int[] origin = ORIGINS[n];
            String target = targets[n];
            add.add("fa reset " + n, () -> {
                found = null;
                shape = null;
                command.accept("execute in " + dimension + " run forceload remove all");
            }, 20);
            add.add("fa locate " + n, () -> onServer(sp -> {
                ServerLevel level = (ServerLevel)sp.level();
                Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("theabyss", target)));
                Pair<BlockPos, Holder<Structure>> pair = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder),
                    new BlockPos(origin[0], 0, origin[1]), 60, false);
                found = pair == null ? null : pair.getFirst();
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] {} located at {}", target, found);
                check("a giant frost arch exists", found != null);
                if (found != null) {
                    command.accept("execute in " + dimension + " run forceload add " + (found.getX() - 150) + " " + (found.getZ() - 150) + " "
                        + (found.getX() + 150) + " " + (found.getZ() + 150));
                    command.accept("execute in " + dimension + " run tp @s " + found.getX() + " 260 " + found.getZ() + " 0 60");
                }
            }), 2400);
            add.add("fa inspect " + n, () -> onServer(sp -> {
                if (found == null) {
                    return;
                }
                ServerLevel level = (ServerLevel)sp.level();
                Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("theabyss", target)));
                StructureStart start = level.getChunk(found.getX() >> 4, found.getZ() >> 4).getStartForStructure(holder.value());
                for (StructurePiece piece : start == null ? List.<StructurePiece>of() : start.getPieces()) {
                    if (piece instanceof GiantArchPiece arch) {
                        shape = arch.shape();
                    }
                }
                check("the arch start has its piece", shape != null);
                if (shape != null) {
                    shapes.add(shape);
                }
                if (shape == null) {
                    return;
                }
                double span = Math.sqrt(Math.pow(shape.bx - shape.ax, 2) + Math.pow(shape.bz - shape.az, 2));
                int crownX = (int)Math.floor(shape.curveX(0.5));
                int crownY = (int)Math.floor(shape.curveY(0.5));
                int crownZ = (int)Math.floor(shape.curveZ(0.5));
                int solid = 0;
                for (int dy = -3; dy <= 3; dy++) {
                    if (!level.getBlockState(new BlockPos(crownX, crownY + dy, crownZ)).isAir()) {
                        solid++;
                    }
                }
                int under = 0;
                for (int y = crownY - 8; y > crownY - 40; y--) {
                    if (level.getBlockState(new BlockPos(crownX, y, crownZ)).isAir()) {
                        under++;
                    }
                }
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frost arch span {} feet {} {} {} / {} {} {} rise {} rTop {} rLeg {} crown {} {} {} solid {} air below {} box {}",
                    Math.round(span), shape.ax, shape.ay, shape.az, shape.bx, shape.by, shape.bz, shape.rise, shape.rTop, shape.rLeg, crownX, crownY, crownZ,
                    solid, under, start.getBoundingBox());
                check("rock at the crown (" + solid + "/7)", solid >= 3);
                check("open space under the crown (" + under + ")", under >= 6);
                check("span at least 70 (" + Math.round(span) + ")", span >= 69);
            }), 20);
        }
        add.add("fa first", () -> shape = shapes.isEmpty() ? null : shapes.get(0), 2);
        add.add("fa side", () -> {
            if (shape == null) {
                return;
            }
            clearView = true;
            view(command, 1.15, 0.35);
        }, 80);
        add.add("fa shot side", () -> shot.accept("127_frostarch_side"), 6);
        add.add("fa side 2", () -> {
            if (shape != null) {
                view(command, -0.9, 0.15);
            }
        }, 80);
        add.add("fa shot side 2", () -> shot.accept("127_frostarch_side_2"), 6);
        add.add("fa below", () -> {
            if (shape != null) {
                double sx = -(shape.bz - shape.az);
                double sz = shape.bx - shape.ax;
                double len = Math.sqrt(sx * sx + sz * sz);
                double x = shape.curveX(0.5) + sx / len * 30.0;
                double z = shape.curveZ(0.5) + sz / len * 30.0;
                double y = Math.max(shape.ay, shape.by) + 4;
                look(command, x, y, z, shape.curveX(0.5), shape.curveY(0.5), shape.curveZ(0.5));
            }
        }, 80);
        add.add("fa shot below", () -> shot.accept("127_frostarch_below"), 6);
        add.add("fa storm", () -> {
            clearView = false;
            if (shape != null) {
                view(command, 0.7, 0.1);
            }
        }, 80);
        add.add("fa shot storm", () -> shot.accept("127_frostarch_storm"), 6);
        add.add("fa result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT frostarch {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void view(Consumer<String> command, double distance, double lift) {
        double span = Math.sqrt(Math.pow(shape.bx - shape.ax, 2) + Math.pow(shape.bz - shape.az, 2));
        double sx = -(shape.bz - shape.az) / span;
        double sz = (shape.bx - shape.ax) / span;
        double cx = shape.curveX(0.5);
        double cz = shape.curveZ(0.5);
        double midY = (Math.min(shape.ay, shape.by) + shape.curveY(0.5)) / 2.0;
        double x = cx + sx * span * distance;
        double z = cz + sz * span * distance;
        double y = midY + span * lift;
        look(command, x, y, z, cx, midY, cz);
    }

    private static void look(Consumer<String> command, double x, double y, double z, double tx, double ty, double tz) {
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        command.accept(String.format(java.util.Locale.ROOT, "execute in " + dimension + " run tp @s %.1f %.1f %.1f %.1f %.1f", x, y, z, yaw, pitch));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frostarch check {}: {}", name, ok ? "ok" : "FAILED");
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
