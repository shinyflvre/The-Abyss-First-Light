package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.entity.InfectedPhantomEntity;

final class PhantomAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static final List<String> hits = new ArrayList<>();
    private static final Set<Integer> seen = new TreeSet<>();
    private static int checks;
    private static boolean watching;

    private PhantomAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) -> {
            if (e.getEntity() instanceof ServerPlayer) {
                hits.add(e.getSource().getMsgId() + " " + e.getNewDamage());
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] phantom player damage {} from {}", e.getNewDamage(), e.getSource().getMsgId());
            }
        });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Post e) -> {
            if (watching && e.getEntity() instanceof InfectedPhantomEntity p && !p.level().isClientSide() && p.attack() != InfectedPhantomEntity.NONE) {
                seen.add((int)p.attack());
            }
        });
        add.add("ph arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -32 -32 32 32");
            command.accept("execute in minecraft:overworld run fill -24 199 -24 24 199 24 minecraft:deepslate_tiles");
            command.accept("execute in minecraft:overworld run fill -24 200 -24 24 214 24 minecraft:air");
            command.accept("time set midnight");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule doMobSpawning false");
            command.accept("kill @e[type=theabyss:infected_phantom]");
        }, 60);
        add.add("ph player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -6 0 -8");
            command.accept("gamemode survival @s");
            command.accept("attribute @s minecraft:generic.max_health base set 200");
            command.accept("effect clear @s");
            onServer(sp -> sp.setHealth(sp.getMaxHealth()));
        }, 40);
        add.add("ph summon", () -> command.accept("execute in minecraft:overworld run summon theabyss:infected_phantom 0 201 6 {Rotation:[180f,0f],PersistenceRequired:1b}"), 20);
        add.add("ph hold", () -> phantom((p, sp) -> {
            p.devHold = true;
            p.setTarget(sp);
        }), 40);
        add.add("ph shot idle", () -> shot.accept("114_phantom_0_idle"), 4);

        add.add("ph venom", () -> phantom((p, sp) -> p.devAttack = InfectedPhantomEntity.VENOM), 9);
        add.add("ph shot venom charge", () -> shot.accept("114_phantom_1_venom_charge"), 8);
        add.add("ph shot venom bolt", () -> shot.accept("114_phantom_2_venom_bolt"), 3);
        add.add("ph shot venom bolt 2", () -> shot.accept("114_phantom_3_venom_bolt_near"), 4);
        add.add("ph shot venom splash", () -> shot.accept("114_phantom_4_venom_splash"), 20);
        add.add("ph venom check", () -> onServer(sp -> {
            check("venom hit (" + hits + ")", hits.stream().anyMatch(h -> h.startsWith("indirectMagic")));
            check("venom poison", sp.hasEffect(MobEffects.POISON));
            hits.clear();
            sp.removeAllEffects();
            sp.setHealth(sp.getMaxHealth());
        }), 20);

        add.add("ph miasma pos", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 3.2 0 -20");
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }, 20);
        add.add("ph miasma", () -> phantom((p, sp) -> p.devAttack = InfectedPhantomEntity.MIASMA), 11);
        add.add("ph shot miasma gather", () -> shot.accept("114_phantom_5_miasma_gather"), 9);
        add.add("ph shot miasma burst", () -> shot.accept("114_phantom_6_miasma_burst"), 4);
        add.add("ph shot miasma wave", () -> shot.accept("114_phantom_7_miasma_wave"), 20);
        add.add("ph shot miasma cloud", () -> shot.accept("114_phantom_8_miasma_cloud"), 4);
        add.add("ph miasma check", () -> onServer(sp -> {
            check("miasma hit (" + hits + ")", hits.stream().anyMatch(h -> h.startsWith("indirectMagic")));
            MobEffectInstance poison = sp.getEffect(MobEffects.POISON);
            check("miasma poison II", poison != null && poison.getAmplifier() >= 1);
            hits.clear();
            sp.removeAllEffects();
            sp.setHealth(sp.getMaxHealth());
        }), 60);

        add.add("ph rend pos", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -4 0 -8");
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            phantom((p, sp) -> {
                p.teleportTo(0, 201, 6);
                sp.removeAllEffects();
            });
        }, 30);
        add.add("ph rend", () -> phantom((p, sp) -> p.devAttack = InfectedPhantomEntity.REND), 4);
        add.add("ph shot rend dissolve", () -> shot.accept("114_phantom_9_rend_dissolve"), 2);
        add.add("ph rend front", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT), 8);
        add.add("ph shot rend rift", () -> shot.accept("114_phantom_10_rend_rift"), 6);
        add.add("ph shot rend emerge", () -> shot.accept("114_phantom_11_rend_emerge"), 4);
        add.add("ph shot rend lunge", () -> shot.accept("114_phantom_12_rend_lunge"), 3);
        add.add("ph shot rend through", () -> shot.accept("114_phantom_13_rend_through"), 20);
        add.add("ph rend check", () -> onServer(sp -> {
            check("rend hit (" + hits + ")", hits.stream().anyMatch(h -> h.startsWith("mob")));
            check("rend slowness", sp.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
            hits.clear();
            sp.removeAllEffects();
            sp.setHealth(sp.getMaxHealth());
        }), 10);

        add.add("ph free", () -> {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            command.accept("execute in minecraft:overworld run tp @s 0 200 -2 0 -8");
            phantom((p, sp) -> {
                p.teleportTo(0, 201, 6);
                p.devHold = false;
            });
            watching = true;
        }, 300);
        add.add("ph free shot", () -> shot.accept("114_phantom_14_free"), 4);
        add.add("ph free check", () -> {
            watching = false;
            check("chooses attacks on its own " + seen, !seen.isEmpty());
            phantom((p, sp) -> check("still alive and hovering", p.isAlive()));
        }, 4);
        add.add("ph result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT phantom {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("ph cleanup", () -> {
            command.accept("kill @e[type=theabyss:infected_phantom]");
            command.accept("effect clear @s");
            command.accept("attribute @s minecraft:generic.max_health base set 20");
        }, 5);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] phantom check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private interface PhantomAction {
        void run(InfectedPhantomEntity phantom, ServerPlayer player);
    }

    private static void phantom(PhantomAction action) {
        onServer(sp -> {
            for (InfectedPhantomEntity p : sp.serverLevel().getEntitiesOfClass(InfectedPhantomEntity.class, new AABB(sp.blockPosition()).inflate(48))) {
                action.run(p, sp);
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
