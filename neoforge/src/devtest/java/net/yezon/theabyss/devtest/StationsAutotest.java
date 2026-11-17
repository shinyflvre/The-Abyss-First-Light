package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.events.ResearchTableHandlerEvents;
import net.yezon.theabyss.network.TheabyssModVariables;

final class StationsAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final BlockPos ARCANE = new BlockPos(-4, 200, 4);
    private static final BlockPos MORTAR = new BlockPos(-2, 200, 4);
    private static final BlockPos INFUSER = new BlockPos(0, 200, 4);
    private static final BlockPos EXTRACTOR = new BlockPos(2, 200, 4);
    private static final BlockPos CUTTER = new BlockPos(4, 200, 4);
    private static final BlockPos RESEARCH = new BlockPos(6, 200, 4);

    private StationsAutotest() {
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.contains(":") ? id : "theabyss:" + id));
    }

    private static ItemStack stack(String id, int count) {
        return new ItemStack(item(id), count);
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("st arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -12 199 -12 12 199 12 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -12 200 -12 12 210 12 minecraft:air");
            command.accept("time set noon");
            command.accept("gamerule doMobSpawning false");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5 0 30");
            command.accept("gamemode survival @s");
            command.accept("clear @s");
            command.accept("execute in minecraft:overworld run setblock -4 200 4 theabyss:arcane_workbench");
            command.accept("execute in minecraft:overworld run setblock -2 200 4 theabyss:mortar_and_pestle");
            command.accept("execute in minecraft:overworld run setblock 0 200 4 theabyss:somnium_infuser");
            command.accept("execute in minecraft:overworld run setblock 2 200 4 theabyss:somnium_extractor");
            command.accept("execute in minecraft:overworld run setblock 4 200 4 theabyss:crystal_cutter");
            command.accept("execute in minecraft:overworld run setblock 6 200 4 theabyss:research_table");
            command.accept("execute in minecraft:overworld run setblock 2 201 4 minecraft:hopper[facing=down]");
            command.accept("execute in minecraft:overworld run setblock 2 199 4 minecraft:hopper[facing=down]");
        }, 40);

        add.add("st mortar fill", () -> onServer(sp -> {
            Container c = container(sp, MORTAR);
            c.setItem(1, stack("vitae_nugget", 3));
            c.setItem(2, stack("loran", 3));
            c.setItem(3, stack("loran", 3));
            c.setItem(4, stack("vitae_nugget", 3));
            c.setItem(5, stack("bottle_of_somnium", 3));
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(MORTAR), MORTAR);
        }), 10);
        add.add("st mortar shot", () -> shot.accept("107_stations_1_mortar_preview"), 2);
        add.add("st mortar shift", () -> mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 0, 0, ClickType.QUICK_MOVE, mc.player), 6);
        add.add("st mortar check", () -> onServer(sp -> {
            int powder = sp.getInventory().countItem(item("ardor_powder"));
            check("mortar: shift-click makes 3 x 2 ardor powder from mixed outer slots (" + powder + ")", powder == 6);
            check("mortar: ingredients used up", container(sp, MORTAR).getItem(5).isEmpty() && container(sp, MORTAR).getItem(1).isEmpty());
            sp.closeContainer();
        }), 4);

        add.add("st arcane fill", () -> onServer(sp -> {
            Container c = container(sp, ARCANE);
            c.setItem(0, stack("ring_of_ring", 1));
            for (int i = 1; i <= 8; i++) {
                c.setItem(i, stack(i % 2 == 1 ? "vitae_nugget" : "aurel_powder", 1));
            }
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(ARCANE), ARCANE);
        }), 10);
        add.add("st arcane shot", () -> shot.accept("107_stations_2_arcane_preview"), 2);
        add.add("st arcane take", () -> mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 9, 0, ClickType.PICKUP, mc.player), 6);
        add.add("st arcane check", () -> onServer(sp -> {
            check("arcane: a turned ring still crafts the Ring of Regeneration (carried " + sp.containerMenu.getCarried() + ")",
                sp.containerMenu.getCarried().is(item("ring_of_reg")));
            check("arcane: the middle ring was used", container(sp, ARCANE).getItem(0).isEmpty());
            sp.getInventory().add(sp.containerMenu.getCarried().copy());
            sp.containerMenu.setCarried(ItemStack.EMPTY);
            sp.closeContainer();
        }), 4);

        add.add("st infuser fill", () -> onServer(sp -> {
            Container c = container(sp, INFUSER);
            for (int i = 1; i <= 4; i++) {
                c.setItem(i, stack("lurker_sobber", 2));
            }
            c.setItem(5, stack("somnium", 2));
            ItemStack energy = stack("loran_energy", 1);
            energy.setDamageValue(40);
            c.setItem(6, energy);
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(INFUSER), INFUSER);
        }), 60);
        add.add("st infuser shot 1", () -> shot.accept("107_stations_3a_infuser_brewing_early"), 70);
        add.add("st infuser shot 2", () -> shot.accept("107_stations_3b_infuser_brewing_late"), 2);
        add.add("st infuser half", () -> onServer(sp -> check("infuser: nothing done before the brew time (result " + container(sp, INFUSER).getItem(0) + ")",
            container(sp, INFUSER).getItem(0).isEmpty())), 70);
        add.add("st infuser close", () -> onServer(ServerPlayer::closeContainer), 220);
        add.add("st infuser check", () -> onServer(sp -> {
            ItemStack result = container(sp, INFUSER).getItem(0);
            ItemStack energy = container(sp, INFUSER).getItem(6);
            check("infuser: two brews in 2 x 200 ticks with the menu closed, Loran Energy 40 -> 42 of 100 used (result " + result + ", energy "
                + energy.getDamageValue() + "/" + energy.getMaxDamage() + ", inputs left " + container(sp, INFUSER).getItem(1).getCount() + ")",
                result.is(item("lurker_juice")) && result.getCount() == 2 && energy.is(item("loran_energy")) && energy.getDamageValue() == 42
                    && container(sp, INFUSER).getItem(1).isEmpty());
            check("infuser: an Incorythe Gem is no energy", !container(sp, INFUSER).canPlaceItem(6, stack("incorythe_gem", 1)));
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(INFUSER), INFUSER);
        }), 6);
        add.add("st infuser take", () -> mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 0, 0, ClickType.QUICK_MOVE, mc.player), 6);
        add.add("st infuser take check", () -> onServer(sp -> {
            check("infuser: shift-click takes the brew (" + sp.getInventory().countItem(item("lurker_juice")) + ")",
                sp.getInventory().countItem(item("lurker_juice")) == 2);
            sp.closeContainer();
        }), 4);

        add.add("st extractor fill", () -> onServer(sp -> {
            container(sp, EXTRACTOR).setItem(2, stack("loran_energy", 1));
            if (sp.serverLevel().getBlockEntity(EXTRACTOR.above()) instanceof Container hopper) {
                hopper.setItem(0, stack("loran", 3));
            }
        }), 170);
        add.add("st extractor check", () -> onServer(sp -> {
            int below = sp.serverLevel().getBlockEntity(EXTRACTOR.below()) instanceof Container hopper ? count(hopper, item("somnium")) : -1;
            int inside = container(sp, EXTRACTOR).getItem(1).getCount();
            int damage = container(sp, EXTRACTOR).getItem(2).getDamageValue();
            check("extractor: 3 Loran -> 3 Somnium with the menu closed, in and out by hoppers (below " + below + ", inside " + inside
                + ", energy used " + damage + ")", below + inside == 3 && damage == 3);
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(EXTRACTOR), EXTRACTOR);
            container(sp, EXTRACTOR).setItem(0, stack("loran", 8));
        }), 22);
        add.add("st extractor shot", () -> shot.accept("107_stations_4_extractor_working"), 2);
        add.add("st extractor close", () -> onServer(ServerPlayer::closeContainer), 2);

        add.add("st cutter fill", () -> onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, v -> {
                v.FrostCrystalResearch = false;
                v.syncPlayerVariables(sp);
            });
            container(sp, CUTTER).setItem(0, stack("frost_crystal_ore", 6));
            container(sp, CUTTER).setItem(2, stack("loran_energy", 1));
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(CUTTER), CUTTER);
        }), 50);
        add.add("st cutter shot locked", () -> shot.accept("107_stations_5_cutter_needs_research"), 2);
        add.add("st cutter locked check", () -> onServer(sp -> {
            check("cutter: an unresearched crystal is not cut (" + container(sp, CUTTER).getItem(0).getCount() + " ore left)",
                container(sp, CUTTER).getItem(0).getCount() == 6);
            sp.closeContainer();
            sp.giveExperienceLevels(10);
            container(sp, RESEARCH).setItem(0, stack("frost_crystal_ore", 1));
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(RESEARCH), RESEARCH);
        }), 6);
        add.add("st research", () -> onServer(sp -> {
            ResearchTableHandlerEvents.execute(sp);
            check("research: costs 5 levels and marks the crystal (level " + sp.experienceLevel + ")",
                TheabyssModVariables.getPlayerVariables(sp).FrostCrystalResearch && sp.experienceLevel == 6);
            sp.closeContainer();
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(CUTTER), CUTTER);
        }), 130);
        add.add("st cutter shot", () -> shot.accept("107_stations_6_cutter_working"), 2);
        add.add("st cutter check", () -> onServer(sp -> {
            int left = container(sp, CUTTER).getItem(0).getCount();
            check("cutter: after the research it cuts (" + left + " ore left, " + container(sp, CUTTER).getItem(1) + ")", left <= 4);
            sp.closeContainer();
        }), 4);
        gemTest(add, shot, "extractor", EXTRACTOR, "loran");
        gemTest(add, shot, "cutter", CUTTER, "frost_crystal_ore");
        add.add("st gem refused", () -> onServer(sp -> {
            check("extractor and cutter: an Incorythe Gem is no energy", !container(sp, EXTRACTOR).canPlaceItem(2, stack("incorythe_gem", 1))
                && !container(sp, CUTTER).canPlaceItem(2, stack("incorythe_gem", 1)));
        }), 2);
        add.add("st result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT stations {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void gemTest(PossessionAutotest.StepAdder add, Consumer<String> shot, String name, BlockPos pos, String input) {
        add.add("st gem " + name + " fresh", () -> onServer(sp -> {
            Container c = container(sp, pos);
            c.setItem(0, stack(input, 3));
            c.setItem(1, ItemStack.EMPTY);
            c.setItem(2, stack("loran_energy", 1));
            check(name + ": the energy slot takes no second item", !c.canPlaceItem(2, stack("loran_energy", 1)));
        }), 130);
        add.add("st gem " + name + " fresh check", () -> onServer(sp -> {
            Container c = container(sp, pos);
            ItemStack gem = c.getItem(2);
            check(name + ": 3 tasks use 3 of 100 and the Loran Energy stays (" + gem + " " + gem.getDamageValue() + "/" + gem.getMaxDamage()
                + ", input left " + c.getItem(0).getCount() + ")", gem.is(item("loran_energy")) && gem.getCount() == 1
                && gem.getDamageValue() == 3 && gem.getMaxDamage() == 100 && c.getItem(0).isEmpty());
            gem.set(net.minecraft.core.component.DataComponents.DAMAGE, 99);
            c.setItem(2, gem);
            c.setItem(0, stack(input, 3));
            c.setItem(1, ItemStack.EMPTY);
        }), 130);
        add.add("st gem " + name + " last check", () -> onServer(sp -> {
            Container c = container(sp, pos);
            check(name + ": the 100th task breaks the Loran Energy, then nothing runs (energy " + c.getItem(2) + ", input left " + c.getItem(0).getCount() + ")",
                c.getItem(2).isEmpty() && c.getItem(0).getCount() == 2);
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(pos), pos);
        }), 8);
        add.add("st gem " + name + " shot", () -> shot.accept("107_stations_7_" + name + "_no_energy"), 2);
        add.add("st gem " + name + " close", () -> onServer(sp -> {
            sp.closeContainer();
            container(sp, pos).setItem(0, ItemStack.EMPTY);
        }), 2);
    }

    private static Container container(ServerPlayer sp, BlockPos pos) {
        return (Container)sp.serverLevel().getBlockEntity(pos);
    }

    private static int count(Container c, Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) {
            if (c.getItem(i).is(item)) {
                n += c.getItem(i).getCount();
            }
        }
        return n;
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] stations check {}: {}", name, ok ? "ok" : "FAILED");
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
