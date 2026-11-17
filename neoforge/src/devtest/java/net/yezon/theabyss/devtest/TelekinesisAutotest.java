package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.client.fx.NightbladeParryClient;
import net.yezon.theabyss.client.fx.TelekinesisClient;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.ComeHere;

final class TelekinesisAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static final Map<Integer, Double> startY = new HashMap<>();
    private static int checks;

    private TelekinesisAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("tele arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -24 -24 24 24");
            command.accept("execute in minecraft:overworld run fill -20 199 -20 20 199 20 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -20 200 -20 20 215 20 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule doMobSpawning false");
            command.accept("kill @e[type=minecraft:husk]");
        }, 60);
        add.add("tele player", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -5 0 -12");
            command.accept("gamemode creative @s");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_telekinetic");
            command.accept("execute in minecraft:overworld run summon minecraft:husk 0.5 200 4.5 {PersistenceRequired:1b,Health:20f}");
            command.accept("execute in minecraft:overworld run summon minecraft:husk 4.5 200 8.5 {PersistenceRequired:1b,Health:20f}");
            command.accept("execute in minecraft:overworld run summon minecraft:husk -3.5 200 11.5 {PersistenceRequired:1b,Health:20f}");
        }, 30);
        add.add("tele prepare", () -> onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, vars -> {
                vars.Mana = 100.0;
                vars.syncPlayerVariables(sp);
            });
            sp.getCooldowns().removeCooldown(sp.getMainHandItem().getItem());
            startY.clear();
            for (Husk h : husks(sp)) {
                startY.put(h.getId(), h.getY());
            }
        }), 10);
        add.add("tele cast", () -> mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND), 5);
        add.add("tele shot chains", () -> shot.accept("103_tele_1_chains_thrown"), 15);
        add.add("tele shot lift", () -> shot.accept("103_tele_2_lifting"), 18);
        add.add("tele shot hang", () -> shot.accept("103_tele_3_hanging"), 2);
        add.add("tele hang check", () -> {
            check("darkness falls (" + TelekinesisClient.darkness() + ")", TelekinesisClient.darkness() > 0.5F);
            onServer(sp -> {
                List<Husk> hs = husks(sp);
                List<String> rise = hs.stream().map(h -> String.format("%.1f", h.getY() - startY.getOrDefault(h.getId(), h.getY()))).toList();
                check("all three hang at the top " + rise, hs.size() == 3
                    && hs.stream().allMatch(h -> h.getY() - startY.getOrDefault(h.getId(), 999.0) >= 5.0));
            });
        }, 9);
        add.add("tele shot slam", () -> shot.accept("103_tele_4_slam"), 3);
        add.add("tele shot impact", () -> shot.accept("103_tele_5_impact"), 25);
        add.add("tele land check", () -> onServer(sp -> {
            List<Husk> hs = husks(sp);
            List<String> state = hs.stream().map(h -> String.format("hp %.1f dy %.1f", h.getHealth(), h.getY() - startY.getOrDefault(h.getId(), h.getY()))).toList();
            check("all three slammed down and hurt " + state, hs.size() == 3
                && hs.stream().allMatch(h -> h.getHealth() <= 12.5F && Math.abs(h.getY() - startY.getOrDefault(h.getId(), 999.0)) < 1.5));
        }), 5);
        add.add("come arena", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -5 0 2");
            command.accept("ta2 somnium level damage 1");
        }, 10);
        add.add("come husk", () -> command.accept("execute in minecraft:overworld run summon minecraft:husk 0.5 200 15.5 {PersistenceRequired:1b,Health:20f}"), 10);
        add.add("come prepare", () -> prepare(), 4);
        add.add("come locked", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 30);
        add.add("come locked check", () -> onServer(sp -> {
            List<Husk> hs = husks(sp);
            check("no pull below damage level 2 " + hs.stream().map(h -> String.format("z %.1f", h.getZ())).toList(),
                hs.size() == 1 && hs.get(0).getZ() > 14.0);
        }), 4);
        add.add("come level 2", () -> command.accept("ta2 somnium level damage 2"), 10);
        add.add("come pull", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 14);
        add.add("come shot pull", () -> shot.accept("103_tele_6_come_here"), 26);
        add.add("come pull check", () -> onServer(sp -> {
            List<Husk> hs = husks(sp);
            check("the chain yanked the husk to the caster, no damage " + hs.stream().map(h -> String.format("z %.1f hp %.1f", h.getZ(), h.getHealth())).toList(),
                hs.size() == 1 && hs.get(0).getZ() < 0.0 && hs.get(0).getHealth() >= 19.5F);
        }), 4);
        add.add("come level 4", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            command.accept("ta2 somnium level damage 4");
        }, 10);
        add.add("come husk 2", () -> command.accept("execute in minecraft:overworld run summon minecraft:husk 0.5 200 15.5 {PersistenceRequired:1b,Health:20f}"), 10);
        add.add("come prepare 2", () -> prepare(), 4);
        add.add("come parry cast", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 14);
        add.add("come shot ring", () -> shot.accept("103_tele_7_parry_ring"), 3);
        add.add("come parry click", () -> check("the left click counts as a parry while pulling", NightbladeParryClient.tryParry()), 1);
        add.add("come shot hit", () -> shot.accept("103_tele_8_parry_hit"), 20);
        add.add("come parry check", () -> onServer(sp -> {
            List<Husk> hs = husks(sp);
            check("the parried blow hits as hard as the slam (18 at level 4) " + hs.stream().map(h -> String.format("hp %.1f", h.getHealth())).toList(),
                hs.isEmpty() || hs.get(0).getHealth() <= 2.5F);
        }), 4);
        add.add("come reset", () -> command.accept("ta2 somnium level reset"), 4);
        add.add("tele result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT telekinesis {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("tele cleanup", () -> command.accept("kill @e[type=minecraft:husk]"), 5);
    }

    private static void prepare() {
        onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, vars -> {
                vars.Mana = 100.0;
                vars.syncPlayerVariables(sp);
            });
            ComeHere.resetCooldown(sp);
        });
    }

    private static List<Husk> husks(ServerPlayer sp) {
        return sp.serverLevel().getEntitiesOfClass(Husk.class, new AABB(sp.blockPosition()).inflate(40));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] tele check {}: {}", name, ok ? "ok" : "FAILED");
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
