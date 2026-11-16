package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalNeedleEntity;
import net.yezon.theabyss.entity.CrystalSlashEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.network.TheabyssModVariables;

final class CrystalRingAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static float playerHealth;

    private CrystalRingAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("ring arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -24 -24 24 24");
            command.accept("execute in minecraft:overworld run fill -20 199 -20 20 199 20 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -20 200 -20 20 212 20 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule spawn_mobs false");
        }, 60);
        add.add("ring player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -6 0 0");
            command.accept("gamemode survival @s");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_crystals");
        }, 30);
        summonHusks(add, command);
        prepare(add);
        add.add("ring slash", () -> mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND), 3);
        add.add("ring shot slash flight", () -> shot.accept("102_ring_1a_slash_flight"), 1);
        add.add("ring shot slash", () -> shot.accept("102_ring_1_crystal_slash"), 21);
        add.add("ring slash check", () -> onServer(sp -> {
            List<Husk> husks = husks(sp);
            long cut = husks.stream().filter(h -> Math.abs(h.getHealth() - 17.0F) < 0.01F).count();
            check("slash cuts all three husks for 3 (" + husks.stream().map(h -> String.valueOf(h.getHealth())).toList() + ")", cut == 3);
            check("slash heals 15 % (" + (sp.getHealth() - playerHealth) + ")", sp.getHealth() - playerHealth > 1.0F);
        }), 2);
        summonHusks(add, command);
        prepare(add);
        add.add("ring needles level", () -> command.accept("ta2 somnium level damage 3"), 6);
        add.add("ring needles", () -> KeyMapping.click(net.yezon.theabyss.init.TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 2);
        add.add("ring shot needles flight", () -> shot.accept("102_ring_2a_needles_flight"), 1);
        add.add("ring shot needles", () -> shot.accept("102_ring_2_crystal_needles"), 24);
        add.add("ring needles check", () -> onServer(sp -> {
            List<Husk> husks = husks(sp);
            Husk first = husks.stream().min((a, b) -> Double.compare(a.distanceToSqr(sp), b.distanceToSqr(sp))).orElse(null);
            check("five needles hit the first husk for 10 at damage level 3 (" + (first == null ? "none" : 20.0F - first.getHealth()) + ")",
                first != null && Math.abs(first.getHealth() - 10.0F) < 0.01F);
            check("needles heal 25 % (" + (sp.getHealth() - playerHealth) + ")", sp.getHealth() - playerHealth > 1.0F);
        }), 2);
        add.add("ring level reset", () -> command.accept("ta2 somnium level reset"), 4);
        add.add("ring slash demo", () -> onServer(sp -> {
            CrystalSlashEntity slash = TheabyssModEntities.CRYSTAL_SLASH.get().create(sp.level(), EntitySpawnReason.MOB_SUMMONED);
            if (slash != null) {
                slash.setPos(-7.0, 200.4, -2.0);
                slash.launch(sp, new Vec3(CrystalSlashEntity.SPEED, 0.0, 0.0), 1.0F);
                sp.level().addFreshEntity(slash);
            }
        }), 6);
        add.add("ring shot slash demo", () -> shot.accept("102_ring_1b_slash_from_above"), 3);
        add.add("ring shot slash demo late", () -> shot.accept("102_ring_1c_slash_from_above_wide"), 2);
        add.add("ring needle demo", () -> onServer(sp -> {
            for (int i = 0; i < 5; i++) {
                CrystalNeedleEntity needle = TheabyssModEntities.CRYSTAL_NEEDLE.get().create(sp.level(), EntitySpawnReason.MOB_SUMMONED);
                if (needle != null) {
                    needle.setPos(-12.0 - i * 0.7, 200.8 + i * 0.35, -1.5 + i * 0.2);
                    needle.launch(sp, new Vec3(CrystalNeedleEntity.SPEED, 0.0, 0.0), 1.0F, -36.0F + i * 72.0F);
                    sp.level().addFreshEntity(needle);
                }
            }
        }), 4);
        add.add("ring shot needle demo", () -> shot.accept("102_ring_2b_needles_streaks"), 3);
        add.add("ring result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT crystalring {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("ring cleanup", () -> command.accept("kill @e[type=minecraft:husk]"), 5);
    }

    private static void summonHusks(PossessionAutotest.StepAdder add, Consumer<String> command) {
        add.add("ring husks", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            for (int z : new int[] {0, 3, 6}) {
                command.accept("execute in minecraft:overworld run summon minecraft:husk 0.5 200 " + z + ".5 {NoAI:1b,PersistenceRequired:1b,Health:20f,Rotation:[180f,0f]}");
            }
        }, 20);
    }

    private static void prepare(PossessionAutotest.StepAdder add) {
        add.add("ring prepare", () -> onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, vars -> {
                vars.Mana = 100.0;
                vars.syncPlayerVariables(sp);
            });
            sp.setHealth(8.0F);
            sp.getFoodData().setFoodLevel(12);
            sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
            playerHealth = 8.0F;
        }), 10);
    }

    private static List<Husk> husks(ServerPlayer sp) {
        return sp.level().getEntitiesOfClass(Husk.class, new AABB(sp.blockPosition()).inflate(30));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ring check {}: {}", name, ok ? "ok" : "FAILED");
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
