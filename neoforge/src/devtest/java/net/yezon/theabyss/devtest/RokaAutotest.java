package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.entity.TheRokaEntity;

final class RokaAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static float health;
    private static Vec3 before = Vec3.ZERO;
    private static double peakY;

    private RokaAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) -> {
            if (e.getEntity() instanceof ServerPlayer) {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] roka player damage {} from {}", e.getNewDamage(), e.getSource().getMsgId());
            }
        });
        add.add("roka arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -32 -32 32 32");
            command.accept("execute in minecraft:overworld run fill -30 199 -30 30 199 30 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -30 200 -30 30 214 30 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule doMobSpawning false");
        }, 60);
        add.add("roka player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -4 0 5");
            command.accept("gamemode survival @s");
            command.accept("attribute @s minecraft:generic.max_health base set 100");
            onServer(sp -> sp.setHealth(sp.getMaxHealth()));
            command.accept("item replace entity @s weapon.mainhand with minecraft:iron_sword");
        }, 40);
        add.add("roka summon", () -> command.accept("execute in minecraft:overworld run summon theabyss:the_roka 0 200 6 {Rotation:[180f,0f],PersistenceRequired:1b}"), 30);
        add.add("roka hold", () -> roka(r -> r.devHold(true)), 40);
        add.add("roka shot idle", () -> shot.accept("100_roka_1_idle"), 10);
        add.add("roka size", () -> roka(r -> check("hitbox " + r.getBbWidth() + " x " + r.getBbHeight(), Math.abs(r.getBbHeight() - 2.7F) < 0.01F)), 2);
        add.add("roka walk", () -> {
            command.accept("tp @s 0 200 -22 0 5");
            roka(r -> r.devHold(false));
        }, 16);
        add.add("roka shot walk", () -> shot.accept("100_roka_2_walk"), 4);
        add.add("roka hold again", () -> roka(r -> {
            r.devHold(true);
            r.teleportTo(0, 200, 6);
            r.setYRot(180);
        }), 30);
        add.add("roka third person", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK), 2);

        attack(add, command, shot, "combo", 2.4, 8, "100_roka_3_combo_hook", 12);
        add.add("roka shot uppercut", () -> shot.accept("100_roka_4_combo_uppercut"), 20);
        add.add("roka combo damage", () -> damageCheck("combo (hook 8 + uppercut 16)", 24.0F), 2);

        attack(add, command, shot, "smash", 4.0, 18, "100_roka_5_smash", 20);
        add.add("roka smash damage", () -> damageCheck("horn smash", 16.0F), 2);

        attack(add, command, shot, "charge", 12.0, 30, "100_roka_6_charge_windup", 10);
        add.add("roka track", () -> trackPeak(), 1);
        for (int i = 0; i < 12; i++) {
            add.add("roka track " + i, () -> trackPeak(), 2);
        }
        add.add("roka shot gore", () -> shot.accept("100_roka_7_charge_gore"), 30);
        add.add("roka charge damage", () -> {
            damageCheck("bull charge", 16.0F);
            check("gored high (" + String.format("%.1f", peakY - before.y) + " blocks)", peakY - before.y > 2.5);
        }, 2);

        prepare(add, command, 12.0);
        add.add("roka charge 2", () -> roka(r -> onServer(sp -> r.devAttack("charge", sp))), 34);
        add.add("roka parry click", () -> check("parry click reaches the server (boss near)", net.yezon.theabyss.client.fx.NightbladeParryClient.tryParry()), 14);
        add.add("roka shot parried", () -> shot.accept("100_roka_8_charge_parried"), 20);
        add.add("roka parry check", () -> onServer(sp -> check("parried charge does no damage", sp.getHealth() >= health - 0.01F)), 30);

        attack(add, command, shot, "leap", 10.0, 22, "100_roka_9_leap_air", 20);
        add.add("roka shot land", () -> shot.accept("100_roka_10_leap_land"), 20);
        add.add("roka leap damage", () -> damageCheck("thunder leap", 16.0F), 2);

        attack(add, command, shot, "roar", 5.0, 11, "100_roka_11_roar", 10);
        add.add("roka roar check", () -> {
            onServer(sp -> check("roar slows and weakens", sp.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && sp.hasEffect(MobEffects.WEAKNESS)));
            roka(r -> check("roar enrages him", r.isEnraged()));
        }, 20);
        add.add("roka shot rage", () -> shot.accept("100_roka_12_enraged"), 4);

        add.add("roka camera back", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 2);
        add.add("roka result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT roka {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("roka cleanup", () -> {
            command.accept("kill @e[type=theabyss:the_roka]");
            command.accept("attribute @s minecraft:generic.max_health base set 20");
        }, 5);
    }

    private static void trackPeak() {
        onServer(sp -> peakY = Math.max(peakY, sp.getY()));
    }

    private static void attack(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, double distance, int shotAfter, String shotName, int rest) {
        prepare(add, command, distance);
        add.add("roka " + name, () -> roka(r -> onServer(sp -> r.devAttack(name, sp))), shotAfter);
        add.add("roka shot " + name, () -> shot.accept(shotName), rest);
    }

    private static void prepare(PossessionAutotest.StepAdder add, Consumer<String> command, double distance) {
        add.add("roka prepare", () -> {
            command.accept(String.format(java.util.Locale.ROOT, "execute in minecraft:overworld run tp @s 0 200 %.2f 0 8", 6.0 - distance - 0.5));
            command.accept("effect clear @s");
            roka(r -> {
                r.teleportTo(0, 200, 6);
                r.setYRot(180);
            });
            onServer(sp -> {
                sp.setHealth(sp.getMaxHealth());
                sp.getFoodData().setFoodLevel(12);
                sp.setDeltaMovement(Vec3.ZERO);
            });
        }, 25);
        add.add("roka remember", () -> onServer(sp -> {
            health = sp.getHealth();
            before = sp.position();
            peakY = sp.getY();
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
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] roka check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void roka(Consumer<TheRokaEntity> action) {
        onServer(sp -> {
            for (TheRokaEntity r : sp.serverLevel().getEntitiesOfClass(TheRokaEntity.class, new AABB(sp.blockPosition()).inflate(48))) {
                action.accept(r);
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
