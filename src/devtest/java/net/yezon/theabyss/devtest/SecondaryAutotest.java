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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.secondary.Secondary;

final class SecondaryAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static final Map<Integer, Float> health = new HashMap<>();
    private static int checks;

    private SecondaryAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String filter) {
        add.add("sec arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -32 -32 32 32");
            command.accept("execute in minecraft:overworld run fill -30 199 -30 30 199 30 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -30 200 -30 30 225 30 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule spawn_mobs false");
            command.accept("gamemode creative @s");
            command.accept("ta2 somnium set infinite");
        }, 60);
        if (wants(filter, "meteor")) {
            ability(add, command, shot, "meteor", "theabyss:ring_of_firestorm", "damage 3", "0.5 200 -6 0 4",
                new String[] {"0.5 200 12.5", "3.5 200 13.5", "-2.5 200 11.5"},
                new int[] {5, 14, 26, 31, 40}, new String[] {"1_mark", "2_falling", "3_close", "4_impact", "5_crater"},
                "meteor hurts the husks", hurt -> hurt >= 2);
        }
        if (wants(filter, "void")) {
            ability(add, command, shot, "void", "theabyss:ring_of_blackstrike", "damage 2", "0.5 200 -6 0 6",
                new String[] {"0.5 200 11.5", "5.5 200 13.5", "-4.5 200 12.5", "2.5 200 17.5"},
                new int[] {6, 22, 44, 51, 56}, new String[] {"1_open", "2_pull", "3_pull_late", "4_collapse", "5_ring"},
                "void rift hurts the husks", hurt -> hurt >= 3);
        }
        if (wants(filter, "scatter")) {
            ability(add, command, shot, "scatter", "theabyss:ring_of_firestrike", "damage 2", "0.5 200 -6 0 2",
                new String[] {"-3.5 200 8.5", "-1.5 200 9.5", "0.5 200 10.5", "2.5 200 9.5", "4.5 200 8.5"},
                new int[] {2, 4, 7, 12}, new String[] {"1_fan", "2_flight", "3_hits", "4_after"},
                "scatter shot hits at least three husks", hurt -> hurt >= 3);
        }
        if (wants(filter, "swap")) {
            ability(add, command, shot, "swap", "theabyss:ring_of_teleport", "usage 2", "0.5 200 -6 0 4",
                new String[] {"0.5 200 12.5"}, new int[] {2, 6, 12, 20}, new String[] {"1_arcs", "2_rush", "3_landed", "4_after"},
                "swap trades places", (sp, hurt) -> sp.getZ() > 10.0 && husks(sp).stream().anyMatch(h -> h.getZ() < -4.0), null);
        }
        if (wants(filter, "rewind")) {
            ability(add, command, shot, "rewind", "theabyss:ring_of_time", "usage 4", "-8.5 200 -8 0 10",
                new String[] {}, new int[] {3, 8, 14, 24}, new String[] {"1_thread", "2_flight", "3_back", "4_clock"},
                "rewind brings the player back near the start", (sp, hurt) -> sp.position().distanceTo(new net.minecraft.world.phys.Vec3(-8.5, 200, -8)) < 4.0,
                () -> {
                    for (int i = 1; i <= 10; i++) {
                        double t = i / 10.0;
                        double x = -8.5 + 14.0 * t;
                        double z = -8.0 + 10.0 * Math.sin(t * Math.PI * 0.5);
                        add.add("rewind walk " + i, () -> command.accept(String.format(java.util.Locale.ROOT,
                            "execute in minecraft:overworld run tp @s %.2f 200 %.2f -60 10", x, z)), 10);
                    }
                });
        }
        if (wants(filter, "tidal")) {
            ability(add, command, shot, "tidal", "theabyss:ring_of_ocean", "usage 2", "0.5 200 -10 0 8",
                new String[] {"0.5 200 0.5", "3.5 200 3.5", "-2.5 200 5.5"}, new int[] {4, 12, 22, 33}, new String[] {"1_rise", "2_roll", "3_far", "4_break"},
                "the wave sweeps the husks away", (sp, hurt) -> husks(sp).stream().allMatch(h -> h.getZ() > 6.0), () -> {
                    add.add("tidal fire", () -> command.accept("execute in minecraft:overworld run fill -2 200 2 2 200 2 minecraft:fire"), 4);
                });
        }
        if (wants(filter, "heal")) {
            ability(add, command, shot, "heal", "theabyss:ring_of_reg", "usage 2", "0.5 200 -6 0 20",
                new String[] {}, new int[] {3, 8, 14, 24}, new String[] {"1_column", "2_beams", "3_halo", "4_after"},
                "the pulse heals the tamed wolves", (sp, hurt) -> sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class,
                    new AABB(sp.blockPosition()).inflate(12)).stream().allMatch(w -> w.getHealth() > 6.0F), () -> {
                    add.add("heal wolves", () -> {
                        command.accept("kill @e[type=minecraft:wolf]");
                        command.accept("execute in minecraft:overworld run summon minecraft:wolf 3.5 200 -1.5 {Health:4f,Owner:[I;1,2,3,4],PersistenceRequired:1b}");
                        command.accept("execute in minecraft:overworld run summon minecraft:wolf -2.5 200 -0.5 {Health:4f,Owner:[I;1,2,3,4],PersistenceRequired:1b}");
                    }, 6);
                });
        }
        if (wants(filter, "wall")) {
            ability(add, command, shot, "wall", "theabyss:ring_of_fangs", "usage 2", "0.5 200 -6 0 12",
                new String[] {"0.5 200 6.5", "-2.5 200 7.5", "3.5 200 7.5"}, new int[] {3, 10, 30, 60}, new String[] {"1_ripple", "2_wall", "3_hold", "4_late"},
                "no husk passes the wall", (sp, hurt) -> {
                    boolean ok = husks(sp).stream().allMatch(h -> h.getZ() > -2.5);
                    sp.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                    return ok;
                }, () -> add.add("wall survival", () -> command.accept("gamemode survival @s"), 2));
        }
        if (wants(filter, "hex")) {
            ability(add, command, shot, "hex", "theabyss:ring_of_curse", "damage 2", "0.5 200 -6 0 4",
                new String[] {"0.5 200 10.5", "2.5 200 11.5", "-1.5 200 12.5"}, new int[] {3, 20}, new String[] {"1_bolt", "2_branded"},
                "the hex leaps on when the branded husk dies", (sp, hurt) -> husks(sp).stream().filter(net.yezon.theabyss.ring.secondary.HexMark::marked).count() >= 2,
                null, () -> add.add("hex kill", () -> onServer(sp -> husks(sp).stream().filter(net.yezon.theabyss.ring.secondary.HexMark::marked).findFirst()
                    .ifPresent(h -> h.hurtServer(sp.level(), sp.damageSources().playerAttack(sp), 100.0F))), 4));
        }
        if (wants(filter, "puppet")) {
            ability(add, command, shot, "puppet", "theabyss:ring_of_possession", "damage 3", "0.5 200 -6 0 6",
                new String[] {"0.5 200 8.5", "3.5 200 10.5"}, new int[] {4, 30, 80}, new String[] {"1_seize", "2_hunt", "3_fight"},
                "the puppet attacks the other husk", (sp, hurt) -> hurt >= 1, null);
        }
        if (wants(filter, "overcharge")) {
            ability(add, command, shot, "overcharge", "theabyss:ring_of_thunder", "damage 2", "0.5 200 -6 0 10",
                new String[] {"0.5 200 -3.5"}, new int[] {3, 10}, new String[] {"1_strike", "2_orbs"},
                "a melee hit spends a charge and calls a bolt", (sp, hurt) -> hurt >= 1 && net.yezon.theabyss.ring.secondary.Overcharge.charges(sp) == 2, null,
                () -> add.add("overcharge hit", () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.level.getEntitiesOfClass(Husk.class, mc.player.getBoundingBox().inflate(6)).stream().findFirst()
                        .ifPresent(h -> mc.gameMode.attack(mc.player, h));
                }, 2));
        }
        if (wants(filter, "bring")) {
            ability(add, command, shot, "bring", "theabyss:ring_of_pocket_box", "usage 2", "0.5 200 -6 0 10",
                new String[] {"0.5 200 2.5"}, new int[] {4, 11, 45, 55}, new String[] {"1_tether", "2_gather", "3_arrived", "4_after"},
                "bring along takes the husk into the pocket room", (sp, hurt) -> sp.level().dimension().identifier().toString().equals("theabyss:pocket_dimension")
                    && !husks(sp).isEmpty(), null);
        }
        if (wants(filter, "bring2")) {
            add.add("bring2 setup", () -> {
                command.accept("execute in minecraft:overworld run tp @s 0.5 200 -6 0 25");
                command.accept("gamemode survival @s");
                command.accept("ta2 somnium level usage 2");
                command.accept("kill @e[type=minecraft:wolf]");
                command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_pocket_box");
                command.accept("execute in minecraft:overworld run summon minecraft:wolf 0.5 200 -3.5 {Owner:[I;1,2,3,4],Sitting:1b,PersistenceRequired:1b}");
            }, 20);
            add.add("bring2 prepare", () -> onServer(sp -> {
                Secondary.resetCooldowns(sp);
                sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
            }), 4);
            add.add("bring2 in", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 50);
            add.add("bring2 shot in", () -> shot.accept("105_bring2_1_in"), 2);
            add.add("bring2 check in", () -> onServer(sp -> check("survival: the tamed wolf comes into the box (" + sp.level().dimension().identifier() + ", wolves "
                + sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, new AABB(sp.blockPosition()).inflate(20)).size() + ")",
                sp.level().dimension().identifier().toString().equals("theabyss:pocket_dimension")
                    && !sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, new AABB(sp.blockPosition()).inflate(20)).isEmpty())), 4);
            add.add("bring2 aim back", () -> onServer(sp -> {
                husks(sp).forEach(h -> h.discard());
                Secondary.resetCooldowns(sp);
                sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
                sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, new AABB(sp.blockPosition()).inflate(20)).stream().findFirst()
                    .ifPresent(w -> sp.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, w.position().add(0.0, 0.4, 0.0)));
            }), 6);
            add.add("bring2 out", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 50);
            add.add("bring2 check out", () -> onServer(sp -> check("survival: the wolf comes back out (" + sp.level().dimension().identifier() + ", wolves "
                + sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, new AABB(sp.blockPosition()).inflate(20)).size() + ")",
                sp.level().dimension() == net.minecraft.world.level.Level.OVERWORLD
                    && !sp.level().getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, new AABB(sp.blockPosition()).inflate(20)).isEmpty())), 4);
            add.add("bring2 creative", () -> command.accept("gamemode creative @s"), 2);
        }
        Runnable third = () -> add.add("third person", () -> Minecraft.getInstance().options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK), 2);
        Runnable first = () -> add.add("first person", () -> Minecraft.getInstance().options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON), 2);
        if (wants(filter, "flamedash")) {
            ability(add, command, shot, "flamedash", "theabyss:ring_of_fire", "usage 2", "0.5 200 -8 0 15",
                new String[] {"0.5 200 -2.5", "1.5 200 0.5"}, new int[] {2, 5, 12, 30}, new String[] {"1_burst", "2_ghosts", "3_trail", "4_burning"},
                "the flame dash carries the player forward and burns the husks", (sp, hurt) -> sp.getZ() > -3.0 && hurt >= 1, third, null);
            first.run();
            add.add("flamedash look", () -> command.accept("execute in minecraft:overworld run tp @s 5.5 203 -5 90 40"), 3);
            add.add("flamedash shot side", () -> shot.accept("105_flamedash_5_trail_side"), 2);
        }
        if (wants(filter, "dash")) {
            ability(add, command, shot, "dash", "theabyss:ring_of_speed", "usage 1", "0.5 200 -8 0 15",
                new String[] {}, new int[] {2, 5, 10}, new String[] {"1_leap", "2_ghosts", "3_fade"},
                "the dash leaps about 8 blocks", (sp, hurt) -> sp.getZ() > -3.0, third, null);
            first.run();
        }
        if (wants(filter, "soulstep")) {
            ability(add, command, shot, "soulstep", "theabyss:ring_of_spectral", "usage 3", "0.5 200 -6 0 10",
                new String[] {}, new int[] {2, 6, 14}, new String[] {"1_step", "2_through", "3_after"},
                "soul step goes through the wall", (sp, hurt) -> sp.getZ() > -4.0,
                () -> {
                    add.add("soulstep wall", () -> command.accept("execute in minecraft:overworld run fill -3 200 -4 3 202 -4 minecraft:stone"), 4);
                    third.run();
                }, null);
            first.run();
            add.add("soulstep wall gone", () -> command.accept("execute in minecraft:overworld run fill -3 200 -4 3 202 -4 minecraft:air"), 2);
        }
        if (wants(filter, "ice")) {
            ability(add, command, shot, "ice", "theabyss:ring_of_freeze", "damage 2", "0.5 200 -6 0 6",
                new String[] {"0.5 200 4.5"}, new int[] {3, 8, 40, 96, 104}, new String[] {"1_bolt", "2_closing", "3_locked", "4_cracks", "5_burst"},
                "the ice holds the husk and hurts it when it bursts", (sp, hurt) -> hurt >= 1, null, null);
        }
        if (wants(filter, "blade")) {
            ability(add, command, shot, "blade", "theabyss:ring_of_nightblade", "damage 3", "0.5 200 -6 0 6",
                new String[] {"0.5 200 6.5", "2.5 200 8.5"}, new int[] {4, 9, 16, 24}, new String[] {"1_throw", "2_hit", "3_turn", "4_back"},
                "the thrown blade cuts the husks", (sp, hurt) -> hurt >= 1, null, null);
        }
        if (wants(filter, "recall")) {
            ability(add, command, shot, "recall", "theabyss:ring_of_home", "usage 2", "0.5 200 -6 0 20",
                new String[] {}, new int[] {10, 30}, new String[] {"1_mark", "2_back"},
                "recall brings the player back to the mark", (sp, hurt) -> sp.position().distanceTo(new net.minecraft.world.phys.Vec3(0.5, 200, -5.5)) < 1.5,
                () -> add.add("recall clear", () -> onServer(sp -> sp.getPersistentData().remove("theabyss_recall_point")), 2),
                () -> {
                    add.add("recall walk", () -> command.accept("execute in minecraft:overworld run tp @s 12.5 200 10.5 135 15"), 10);
                    add.add("recall shot beacon", () -> shot.accept("105_recall_1b_beacon_far"), 2);
                    add.add("recall press", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 3);
                    add.add("recall shot flash", () -> shot.accept("105_recall_1c_flash"), 2);
                });
        }
        if (filter != null && List.of(filter.split(",")).contains("stations")) {
            add.add("stations place", () -> {
                command.accept("execute in minecraft:overworld run setblock -2 200 3 theabyss:research_table");
                command.accept("execute in minecraft:overworld run setblock 0 200 3 theabyss:somnium_extractor");
                command.accept("execute in minecraft:overworld run setblock 2 200 3 theabyss:mortar_and_pestle");
                command.accept("execute in minecraft:overworld run tp @s 0.5 201.5 -0.5 -20 35");
            }, 20);
            add.add("stations shot", () -> shot.accept("106_stations_front"), 2);
            add.add("stations side", () -> command.accept("execute in minecraft:overworld run tp @s 3.5 201.8 1.5 60 40"), 10);
            add.add("stations shot 2", () -> shot.accept("106_stations_side"), 2);
        }
        if (wants(filter, "oceanp")) {
            add.add("oceanp pool", () -> {
                command.accept("gamemode survival @s");
                command.accept("execute in minecraft:overworld run fill 10 196 10 14 199 14 minecraft:water");
                command.accept("execute in minecraft:overworld run tp @s 12.5 196 12.5");
                command.accept("item replace entity @s weapon.mainhand with minecraft:stick");
                command.accept("item replace entity @s hotbar.4 with theabyss:ring_of_ocean");
                command.accept("ta2 somnium level reset");
                command.accept("ta2 somnium level usage 2");
            }, 25);
            add.add("oceanp low", () -> onServer(sp -> sp.setAirSupply(100)), 30);
            add.add("oceanp low check", () -> onServer(sp -> check("usage 2: the ring does not keep the air (" + sp.getAirSupply() + ")", sp.getAirSupply() < 100)), 2);
            add.add("oceanp level 4", () -> command.accept("ta2 somnium level usage 4"), 25);
            add.add("oceanp air", () -> onServer(sp -> sp.setAirSupply(100)), 30);
            add.add("oceanp check", () -> onServer(sp -> {
                check("usage 4: no air lost with the ring in the inventory (" + sp.getAirSupply() + ")", sp.getAirSupply() == sp.getMaxAirSupply());
                double swim = sp.getAttributeValue(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
                check("usage 4: 50 % faster in water (" + swim + ")", Math.abs(swim - 1.5) < 1.0E-3);
            }), 2);
            add.add("oceanp cleanup", () -> {
                command.accept("execute in minecraft:overworld run fill 10 196 10 14 199 14 minecraft:air");
                command.accept("gamemode creative @s");
            }, 4);
        }
        add.add("sec tooltips", () -> {
            Minecraft mc = Minecraft.getInstance();
            for (String id : new String[] {"ring_of_time", "ring_of_teleport", "ring_of_thunder"}) {
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("theabyss:" + id));
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                item.appendHoverText(stack, net.minecraft.world.item.Item.TooltipContext.of(mc.level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT,
                    line -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] tooltip {} | {}", id, line.getString()), net.minecraft.world.item.TooltipFlag.NORMAL);
            }
        }, 2);
        add.add("sec result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT secondary {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
        add.add("sec cleanup", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            command.accept("ta2 somnium level reset");
        }, 5);
    }

    private static boolean wants(String filter, String name) {
        return filter == null || filter.isEmpty() || List.of(filter.split(",")).contains(name);
    }

    private static void ability(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, String ring,
                                String level, String tp, String[] husks, int[] shotTicks, String[] shotNames, String check,
                                java.util.function.IntPredicate ok) {
        ability(add, command, shot, name, ring, level, tp, husks, shotTicks, shotNames, check, (sp, hurt) -> ok.test(hurt), null, null);
    }

    interface Check {
        boolean test(ServerPlayer sp, int hurt);
    }

    private static void ability(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, String ring,
                                String level, String tp, String[] husks, int[] shotTicks, String[] shotNames, String check, Check ok,
                                Runnable beforeCast) {
        ability(add, command, shot, name, ring, level, tp, husks, shotTicks, shotNames, check, ok, beforeCast, null);
    }

    private static void ability(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String name, String ring,
                                String level, String tp, String[] husks, int[] shotTicks, String[] shotNames, String check, Check ok,
                                Runnable beforeCast, Runnable afterFirstShot) {
        add.add(name + " setup", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            command.accept("ta2 somnium level reset");
            command.accept("ta2 somnium level " + level);
            command.accept("execute in minecraft:overworld run tp @s " + tp);
            command.accept("item replace entity @s weapon.mainhand with " + ring);
            for (String at : husks) {
                command.accept("execute in minecraft:overworld run summon minecraft:husk " + at + " {PersistenceRequired:1b,Health:20f}");
            }
        }, 20);
        add.add(name + " prepare", () -> onServer(sp -> {
            Secondary.resetCooldowns(sp);
            health.clear();
            for (Husk h : husks(sp)) {
                health.put(h.getId(), h.getHealth());
            }
        }), 4);
        if (beforeCast != null) {
            beforeCast.run();
        }
        add.add(name + " cast", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), shotTicks[0]);
        for (int i = 0; i < shotTicks.length; i++) {
            int next = i + 1 < shotTicks.length ? shotTicks[i + 1] - shotTicks[i] : 6;
            String shotName = "105_" + name + "_" + shotNames[i];
            add.add(name + " shot " + shotNames[i], () -> shot.accept(shotName), Math.max(1, next));
            if (i == 0 && afterFirstShot != null) {
                afterFirstShot.run();
            }
        }
        add.add(name + " check", () -> onServer(sp -> {
            int hurt = 0;
            List<String> state = new ArrayList<>();
            for (Husk h : husks(sp)) {
                Float before = health.get(h.getId());
                if (before != null && h.getHealth() < before) {
                    hurt++;
                }
                state.add(String.format("%.1f", h.getHealth()));
            }
            int dead = (int)health.keySet().stream().filter(id -> sp.level().getEntity(id) == null || !((LivingEntity)sp.level().getEntity(id)).isAlive()).count();
            check(check + " (hurt " + hurt + ", dead " + dead + ", hp " + state + ", player " + String.format("%.1f %.1f %.1f", sp.getX(), sp.getY(), sp.getZ()) + ")",
                ok.test(sp, hurt + dead));
        }), 4);
    }

    private static List<Husk> husks(ServerPlayer sp) {
        return sp.level().getEntitiesOfClass(Husk.class, new AABB(sp.blockPosition()).inflate(50));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] sec check {}: {}", name, ok ? "ok" : "FAILED");
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
