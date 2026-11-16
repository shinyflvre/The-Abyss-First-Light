package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.entity.CrystalGolemEntity;

final class GolemAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static float health;
    private static Vec3 before = Vec3.ZERO;
    private static float golemHealth;

    private GolemAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) -> {
            if (e.getEntity() instanceof ServerPlayer) {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] golem player damage {} from {}", e.getHealthDamage(), e.getSource().getMsgId());
            }
        });
        add.add("golem arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -24 -24 24 24");
            command.accept("execute in minecraft:overworld run fill -24 199 -24 24 199 24 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -24 200 -24 24 212 24 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule spawn_mobs false");
        }, 60);
        add.add("golem player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -4 0 5");
            command.accept("gamemode survival @s");
            command.accept("item replace entity @s weapon.mainhand with minecraft:iron_sword");
        }, 60);
        add.add("golem summon", () -> command.accept("execute in minecraft:overworld run summon theabyss:crystal_golem 0 200 4 {Rotation:[180f,0f],PersistenceRequired:1b}"), 30);
        add.add("golem hold", () -> golem(g -> g.devHold(true)), 40);
        add.add("golem shot idle", () -> shot.accept("98_golem_1_idle"), 20);
        add.add("golem walk", () -> {
            command.accept("tp @s 0 200 -12 0 5");
            golem(g -> g.devHold(false));
        }, 28);
        add.add("golem shot walk", () -> shot.accept("98_golem_2_walk"), 8);
        add.add("golem shot walk 2", () -> shot.accept("98_golem_2_walk_b"), 4);
        add.add("golem hold again", () -> golem(g -> {
            g.devHold(true);
            g.teleportTo(0, 200, 4);
            g.setYRot(180);
        }), 30);

        attack(add, command, shot, "punch", 2.6, 5, "98_golem_3_punch", 12);
        add.add("golem punch damage", () -> damageCheck("punch", 14.0F), 2);
        attack(add, command, shot, "iron_punch", 5.5, 16, "98_golem_4a_iron_glow", 30);
        add.add("golem third person", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK), 2);
        attack(add, command, shot, "iron_punch", 2.8, 13, "98_golem_4_iron_charge", 4);
        for (int i = 0; i < 10; i++) {
            add.add("golem iron track", () -> onServer(sp -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] golem knockback track pos {} delta {} ground {}",
                sp.position(), sp.getDeltaMovement(), sp.onGround())), 2);
        }
        add.add("golem iron shot hit", () -> shot.accept("98_golem_5_iron_hit"), 2);
        add.add("golem iron damage", () -> {
            damageCheck("iron punch", 14.0F);
            onServer(sp -> {
                double distance = new Vec3(sp.getX() - before.x, 0, sp.getZ() - before.z).length();
                check("iron punch knockback about 8 blocks (" + String.format("%.1f", distance) + ")", distance > 6.0 && distance < 11.0);
            });
        }, 2);
        prepare(add, command, 2.8);
        add.add("golem iron 2", () -> golem(g -> onServer(sp -> g.devAttack("iron_punch", sp))), 15);
        add.add("golem parry click", () -> check("parry click reaches the server (boss near)", net.yezon.theabyss.client.fx.NightbladeParryClient.tryParry()), 12);
        add.add("golem shot stagger", () -> shot.accept("98_golem_6_parried_stagger"), 12);
        add.add("golem parry check", () -> onServer(sp -> check("parried iron punch does no damage", sp.getHealth() >= health - 0.01F)), 30);

        attack(add, command, shot, "slam", 3.5, 10, "98_golem_7_slam_raise", 13);
        add.add("golem slam shot impact", () -> shot.accept("98_golem_8_slam_impact"), 3);
        add.add("golem slam shot wave", () -> shot.accept("98_golem_9_slam_wave"), 20);
        add.add("golem slam damage", () -> damageCheck("slam (25 % more, whole half hearts)", 18.0F), 2);
        attack(add, command, shot, "eruption", 9.0, 18, "98_golem_10_eruption_cracks", 6);
        add.add("golem eruption shot spikes", () -> shot.accept("98_golem_11_eruption_spikes"), 30);
        add.add("golem eruption damage", () -> damageCheck("crystal eruption", 14.0F), 2);
        attack(add, command, shot, "throw", 12.0, 22, "98_golem_12_throw_flight", 30);
        add.add("golem throw damage", () -> damageCheck("crystal throw", 14.0F), 2);
        prepare(add, command, 12.0);
        add.add("golem throw 2", () -> golem(g -> {
            golemHealth = g.getHealth();
            onServer(sp -> g.devAttack("throw", sp));
        }), 26);
        add.add("golem throw parry click", () -> check("parry click reaches the server (boss near)", net.yezon.theabyss.client.fx.NightbladeParryClient.tryParry()), 14);
        add.add("golem shot throw parried", () -> shot.accept("98_golem_13_throw_parried_dazed"), 10);
        add.add("golem throw parry check", () -> {
            onServer(sp -> check("parried crystal does not hurt the player", sp.getHealth() >= health - 0.01F));
            golem(g -> check("parried crystal hits the golem (" + (golemHealth - g.getHealth()) + ") (28 minus his armour) and dazes him", golemHealth - g.getHealth() >= 26.0F && g.isDazed()));
        }, 70);
        add.add("golem wall", () -> command.accept("execute in minecraft:overworld run fill -5 200 -8 5 203 -8 minecraft:stone"), 4);
        attack(add, command, shot, "charge", 9.0, 26, "98_golem_14_charge_run", 6);
        add.add("golem charge shot wall", () -> shot.accept("98_golem_15_charge_wall_dazed"), 14);
        add.add("golem charge check", () -> {
            damageCheck("charge", 14.0F);
            golem(g -> check("charge into the wall dazes him", g.isDazed()));
        }, 2);
        add.add("golem wall away", () -> command.accept("execute in minecraft:overworld run fill -5 200 -8 5 203 -8 minecraft:air"), 60);
        add.add("golem no name / boss bar", () -> golem(g -> check("no name tag", !g.hasCustomName() && !g.isCustomNameVisible())), 2);
        add.add("golem camera back", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 2);
        add.add("eagle far golem", () -> {
            command.accept("execute in minecraft:overworld run forceload add 32 -80 80 -32");
            command.accept("execute in minecraft:overworld run fill 50 199 -60 70 199 -40 minecraft:stone");
            command.accept("execute in minecraft:overworld run summon theabyss:crystal_golem 60 200 -50 {PersistenceRequired:1b,NoAI:1b}");
            command.accept("execute in minecraft:overworld run tp @s 0 200 0 0 0");
        }, 60);
        add.add("eagle use", () -> onServer(sp -> net.yezon.theabyss.ring.EagleBossTracker.start(sp, 200)), 20);
        add.add("eagle shot behind", () -> shot.accept("98_golem_16_eagle_compass_behind"), 2);
        add.add("eagle turn", () -> command.accept("tp @s 0 200 0 130 0"), 15);
        add.add("eagle shot ahead", () -> shot.accept("98_golem_17_eagle_compass_ahead"), 2);
        add.add("eagle check", () -> check("eagle compass shows both golems (" + net.yezon.theabyss.client.fx.EagleBossCompass.shown() + ")",
            net.yezon.theabyss.client.fx.EagleBossCompass.shown() == 2), 2);
        add.add("golem result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT golem {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("golem cleanup", () -> command.accept("kill @e[type=theabyss:crystal_golem]"), 5);
    }

    private static void attack(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, double distance, int shotAfter, String shotName, int rest) {
        prepare(add, command, distance);
        add.add("golem " + name, () -> golem(g -> onServer(sp -> g.devAttack(name, sp))), shotAfter);
        add.add("golem shot " + name, () -> shot.accept(shotName), rest);
    }

    private static void prepare(PossessionAutotest.StepAdder add, Consumer<String> command, double distance) {
        add.add("golem prepare", () -> {
            command.accept(String.format(java.util.Locale.ROOT, "execute in minecraft:overworld run tp @s 0 200 %.2f 0 8", 4.0 - distance - 0.3));
            golem(g -> {
                g.teleportTo(0, 200, 4);
                g.setYRot(180);
            });
            onServer(sp -> {
                sp.setHealth(sp.getMaxHealth());
                sp.getFoodData().setFoodLevel(12);
                sp.setDeltaMovement(Vec3.ZERO);
            });
        }, 12);
        add.add("golem remember", () -> onServer(sp -> {
            health = sp.getHealth();
            before = sp.position();
        }), 2);
    }

    private static void damageCheck(String name, float expected) {
        onServer(sp -> {
            float taken = health - sp.getHealth();
            check(name + " damage " + taken + " (expected " + expected + ")", Math.abs(taken - expected) < 0.01F);
        });
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] golem check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void golem(Consumer<CrystalGolemEntity> action) {
        onServer(sp -> {
            for (CrystalGolemEntity g : sp.level().getEntitiesOfClass(CrystalGolemEntity.class, new AABB(sp.blockPosition()).inflate(40))) {
                action.accept(g);
            }
        });
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
