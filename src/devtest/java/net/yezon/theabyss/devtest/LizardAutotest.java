package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.entity.LizardEntity;
import net.yezon.theabyss.init.TheabyssModEntities;

final class LizardAutotest {
    static String prefix = "118_lizard";
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static double climbStart;

    private LizardAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("lz arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -8 199 -8 8 199 8 minecraft:smooth_stone");
            command.accept("execute in minecraft:overworld run fill -8 200 -8 8 212 8 minecraft:air");
            command.accept("time set noon");
            command.accept("gamemode creative @s");
            command.accept("kill @e[type=theabyss:lizard]");
            command.accept("execute in minecraft:overworld run summon theabyss:lizard 0.5 200 0.5 {NoAI:1b,Rotation:[0f,0f],PersistenceRequired:1b}");
        }, 40);
        add.add("lz side", () -> command.accept("execute in minecraft:overworld run tp @s -1.0 200 0.5 -90 55"), 20);
        add.add("lz shot side", () -> shot.accept(prefix + "_side"), 2);
        add.add("lz front", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200 1.9 180 55"), 20);
        add.add("lz shot front", () -> shot.accept(prefix + "_front"), 2);
        add.add("lz top", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200.6 0.5 0 90"), 20);
        add.add("lz shot top", () -> shot.accept(prefix + "_top"), 2);
        add.add("lz three quarter", () -> command.accept("execute in minecraft:overworld run tp @s -0.6 200 -0.6 -45 50"), 20);
        add.add("lz shot three quarter", () -> shot.accept(prefix + "_34"), 2);
        if (prefix.endsWith("old")) {
            return;
        }
        add.add("lz tree", () -> {
            command.accept("kill @e[type=theabyss:lizard]");
            command.accept("execute in minecraft:overworld run fill 3 200 0 3 207 0 minecraft:oak_log");
            onServer(sp -> {
                ServerLevel level = (ServerLevel)sp.level();
                LizardEntity l = TheabyssModEntities.LIZARD.get().create(level, EntitySpawnReason.COMMAND);
                l.snapTo(2.0 + 0.5 + 0.09, 201.5, 0.5, 0.0F, 0.0F);
                l.setPersistenceRequired();
                level.addFreshEntity(l);
                l.setWall(Direction.EAST);
                l.setNoAi(true);
            });
        }, 40);
        add.add("lz wall side", () -> command.accept("execute in minecraft:overworld run tp @s 2.6 200.6 -2.4 0 10"), 20);
        add.add("lz shot wall side", () -> shot.accept(prefix + "_wall_side"), 2);
        add.add("lz wall back", () -> command.accept("execute in minecraft:overworld run tp @s 0.2 200.4 0.5 -90 -5"), 20);
        add.add("lz shot wall back", () -> shot.accept(prefix + "_wall_back"), 2);
        add.add("lz climb ai", () -> lizards(l -> {
            l.setNoAi(false);
            climbStart = l.getY();
        }), 200);
        add.add("lz climb check", () -> lizards(l -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] lizard after 10 s: y {} -> {} wall {}", climbStart, l.getY(), l.wall());
            check("lizard moved on or off the trunk without falling through it (" + l.position() + ")", l.getX() > 1.9 && l.getX() < 3.0 || l.wall() == null);
        }), 2);
        add.add("lz shot climbing", () -> shot.accept(prefix + "_climbing"), 2);
        add.add("lz spawn tree", () -> {
            command.accept("kill @e[type=theabyss:lizard]");
            onServer(sp -> {
                ServerLevel level = (ServerLevel)sp.level();
                int attached = 0;
                for (int i = 0; i < 10; i++) {
                    LizardEntity l = TheabyssModEntities.LIZARD.get().create(level, EntitySpawnReason.NATURAL);
                    l.snapTo(1.5, 200, -1.5, 0.0F, 0.0F);
                    l.finalizeSpawn(level, level.getCurrentDifficultyAt(l.blockPosition()), EntitySpawnReason.NATURAL, null);
                    if (l.wall() != null) {
                        attached++;
                        BlockPos wallBlock = l.blockPosition().relative(l.wall());
                        check("natural tree spawn clings to the log (" + l.position() + " " + l.wall() + ")",
                            level.getBlockState(BlockPos.containing(l.getX(), l.getY() + 0.3, l.getZ()).relative(l.wall())).is(net.minecraft.tags.BlockTags.LOGS));
                    }
                    l.discard();
                }
                check("about half of the natural spawns near a tree cling to it (" + attached + "/10)", attached >= 2 && attached <= 9);
            });
        }, 10);
        add.add("lz walk", () -> {
            command.accept("execute in minecraft:overworld run fill 3 200 0 3 207 0 minecraft:air");
            command.accept("execute in minecraft:overworld run summon theabyss:lizard -3.5 200 0.5 {PersistenceRequired:1b}");
            command.accept("execute in minecraft:overworld run tp @s -3.5 201 3.5 180 35");
        }, 60);
        add.add("lz shot walk", () -> shot.accept(prefix + "_walk"), 2);
        add.add("lz result", LizardAutotest::result, 2);
    }

    private static void lizards(Consumer<LizardEntity> action) {
        onServer(sp -> {
            for (LizardEntity l : sp.level().getEntitiesOfClass(LizardEntity.class, new AABB(sp.blockPosition()).inflate(32))) {
                action.accept(l);
            }
        });
    }

    static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] lizard check {}: {}", name, ok ? "ok" : "FAILED");
    }

    static void result() {
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT lizard {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures);
    }

    static void onServer(Consumer<ServerPlayer> action) {
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
