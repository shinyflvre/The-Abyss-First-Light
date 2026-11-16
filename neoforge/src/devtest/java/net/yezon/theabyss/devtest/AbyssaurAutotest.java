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
import net.yezon.theabyss.entity.AbyssaurEntity;

final class AbyssaurAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static float health;
    private static float dinoHealth;
    private static float lastYaw = Float.NaN;
    private static float maxTurn;
    private static boolean measuring;

    private AbyssaurAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) -> {
            if (e.getEntity() instanceof ServerPlayer) {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] dino player damage {} from {}", e.getHealthDamage(), e.getSource().getMsgId());
            }
        });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Post e) -> {
            if (measuring && e.getEntity() instanceof AbyssaurEntity d && !d.level().isClientSide()) {
                if (!Float.isNaN(lastYaw) && Math.abs(net.minecraft.util.Mth.wrapDegrees(d.getYRot() - lastYaw)) > 8.5F) {
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] dino big turn {} -> {} (start of tick {}) tick {}", lastYaw, d.getYRot(), d.debugTickStartYaw(), d.tickCount);
                }
                if (!Float.isNaN(lastYaw)) {
                    maxTurn = Math.max(maxTurn, Math.abs(net.minecraft.util.Mth.wrapDegrees(d.getYRot() - lastYaw)));
                }
                lastYaw = d.getYRot();
            }
        });
        add.add("dino arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -32 -32 32 32");
            command.accept("execute in minecraft:overworld run fill -30 199 -30 30 199 30 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -30 200 -30 30 214 30 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule spawn_mobs false");
        }, 60);
        add.add("dino player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -8 0 5");
            command.accept("gamemode survival @s");
            command.accept("attribute @s minecraft:max_health base set 100");
            command.accept("item replace entity @s weapon.mainhand with minecraft:iron_sword");
            onServer(sp -> sp.setHealth(sp.getMaxHealth()));
        }, 40);
        add.add("dino summon", () -> command.accept("execute in minecraft:overworld run summon theabyss:abyssaur 0 200 6 {Rotation:[180f,0f],PersistenceRequired:1b}"), 30);
        add.add("dino hold", () -> dino(d -> d.devHold(true)), 40);
        add.add("dino shot idle", () -> shot.accept("101_dino_1_idle"), 10);
        add.add("dino size", () -> dino(d -> check("hitbox " + d.getBbWidth() + " x " + d.getBbHeight(), Math.abs(d.getBbWidth() - 2.2F) < 0.01F)), 2);
        add.add("dino walk", () -> {
            command.accept("tp @s 14 200 6 90 5");
            dino(d -> d.devHold(false));
        }, 2);
        for (int i = 0; i < 40; i++) {
            final int step = i;
            add.add("dino turn " + i, () -> {
                if (step % 10 == 0) {
                    double a = step / 10.0 * Math.PI / 2.0;
                    command.accept(String.format(java.util.Locale.ROOT, "tp @s %.1f 200 %.1f", 14 * Math.cos(a), 6 + 14 * Math.sin(a)));
                }
                measuring = true;
            }, 1);
        }
        add.add("dino shot walk", () -> shot.accept("101_dino_2_walk"), 2);
        add.add("dino turn check", () -> measuring = false, 1);
        add.add("dino turn result", () -> check("turns at most ~6 deg per tick while walking (" + maxTurn + ")", maxTurn <= 8.5F), 2);
        add.add("dino hold again", () -> dino(d -> {
            d.devHold(true);
            d.teleportTo(0, 200, 6);
            d.setYRot(180);
            d.setYBodyRot(180);
            d.setYHeadRot(180);
        }), 30);
        add.add("dino head aim", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 0.6 0 0");
            dino(d -> dinoHealth = d.getHealth());
        }, 20);
        add.add("dino head pick", () -> {
            boolean onHead = mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit && hit.getEntity() instanceof net.yezon.theabyss.neoforge.entity.NeoForgePartEntity;
            check("crosshair finds the head hit box (" + (mc.hitResult == null ? "none" : mc.hitResult.getType()) + ")", onHead);
            if (onHead) {
                mc.gameMode.attack(mc.player, ((net.minecraft.world.phys.EntityHitResult)mc.hitResult).getEntity());
            }
        }, 6);
        add.add("dino head hit", () -> dino(d -> check("a hit on the head hurts him (" + (dinoHealth - d.getHealth()) + ")", d.getHealth() < dinoHealth - 0.5F)), 2);
        add.add("dino third person", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK), 2);

        attack(add, command, shot, "bite", 3.0, 9, "101_dino_3_bite", 16);
        add.add("dino bite damage", () -> damageCheck("bite", 15.0F), 2);

        attack(add, command, shot, "tail_sweep", -3.0, 14, "101_dino_4_tail_sweep", 20);
        add.add("dino tail damage", () -> damageCheck("tail sweep", 15.0F), 2);

        attack(add, command, shot, "breath", 5.0, 20, "101_dino_5_breath", 20);
        add.add("dino breath check", () -> onServer(sp -> {
            float taken = health - sp.getHealth();
            check("breath burns (" + taken + ") and poisons", taken >= 2.0F && sp.hasEffect(MobEffects.POISON));
        }), 2);
        add.add("dino shot pools", () -> shot.accept("101_dino_6_bile_pools"), 4);

        attack(add, command, shot, "lunge", 10.0, 14, "101_dino_7_lunge", 20);
        add.add("dino lunge damage", () -> damageCheck("lunge", 15.0F), 2);

        prepare(add, command, 10.0);
        add.add("dino lunge 2", () -> dino(d -> onServer(sp -> d.devAttack("lunge", sp))), 12);
        add.add("dino parry click", () -> check("parry click reaches the server (boss near)", net.yezon.theabyss.client.fx.NightbladeParryClient.tryParry()), 12);
        add.add("dino shot parried", () -> shot.accept("101_dino_8_lunge_parried"), 20);
        add.add("dino parry check", () -> onServer(sp -> check("parried lunge does no damage", sp.getHealth() >= health - 0.01F)), 20);

        attack(add, command, shot, "roar", 8.0, 14, "101_dino_9_roar", 10);
        add.add("dino roar check", () -> onServer(sp -> check("roar: weakness, hunger, slowness, nausea", sp.hasEffect(MobEffects.WEAKNESS)
            && sp.hasEffect(MobEffects.HUNGER) && sp.hasEffect(MobEffects.SLOWNESS) && sp.hasEffect(MobEffects.NAUSEA))), 20);

        add.add("dino camera back", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 2);
        add.add("dino result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT abyssaur {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("dino cleanup", () -> {
            command.accept("kill @e[type=theabyss:abyssaur]");
            command.accept("effect clear @s");
            command.accept("attribute @s minecraft:max_health base set 20");
        }, 5);
    }

    private static void attack(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, double distance, int shotAfter, String shotName, int rest) {
        prepare(add, command, distance);
        add.add("dino " + name, () -> dino(d -> onServer(sp -> d.devAttack(name, sp))), shotAfter);
        add.add("dino shot " + name, () -> shot.accept(shotName), rest);
    }

    private static void prepare(PossessionAutotest.StepAdder add, Consumer<String> command, double distance) {
        add.add("dino prepare", () -> {
            double z = distance >= 0 ? 6.0 - 1.1 - distance - 0.3 : 6.0 + 1.1 - distance + 0.3;
            command.accept(String.format(java.util.Locale.ROOT, "execute in minecraft:overworld run tp @s 0 200 %.2f %d 8", z, distance >= 0 ? 0 : 180));
            command.accept("effect clear @s");
            dino(d -> {
                d.teleportTo(0, 200, 6);
                d.setYRot(180);
                d.setYBodyRot(180);
                d.setYHeadRot(180);
            });
            onServer(sp -> {
                sp.setHealth(sp.getMaxHealth());
                sp.getFoodData().setFoodLevel(12);
                sp.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            });
        }, 25);
        add.add("dino remember", () -> onServer(sp -> health = sp.getHealth()), 2);
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
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] dino check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void dino(Consumer<AbyssaurEntity> action) {
        onServer(sp -> {
            for (AbyssaurEntity d : sp.level().getEntitiesOfClass(AbyssaurEntity.class, new AABB(sp.blockPosition()).inflate(48))) {
                action.accept(d);
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
