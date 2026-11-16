package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.client.fx.SpellWheelClient;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.GauntletMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.SomniumUpgrades;

final class GauntletAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static double manaBefore;

    private GauntletAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("gauntlet arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -24 -24 24 24");
            command.accept("execute in minecraft:overworld run fill -20 199 -20 20 199 20 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -20 200 -20 20 212 20 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("gamerule spawn_mobs false");
        }, 60);
        add.add("gauntlet give", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -5 0 0");
            command.accept("gamemode survival @s");
            command.accept("item replace entity @s weapon.mainhand with theabyss:infected_gauntlet[minecraft:container=["
                + "{slot:0,item:{id:\"theabyss:ring_of_fire\",count:1}},"
                + "{slot:1,item:{id:\"theabyss:ring_of_crystals\",count:1}},"
                + "{slot:2,item:{id:\"theabyss:ring_of_freeze\",count:1}}]]");
            command.accept("item replace entity @s hotbar.4 with theabyss:ring_of_telekinetic");
        }, 30);
        add.add("gauntlet prepare", () -> onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, vars -> {
                vars.Mana = 100.0;
                vars.syncPlayerVariables(sp);
            });
            manaBefore = 100.0;
        }), 10);
        add.add("upgrades reset", () -> command.accept("ta2 somnium level reset"), 10);
        add.add("upgrades drink", () -> onServer(sp -> {
            for (int i = 0; i < 4; i++) {
                SomniumUpgrades.drink(sp, SomniumUpgrades.Kind.DAMAGE);
            }
            SomniumUpgrades.drink(sp, SomniumUpgrades.Kind.COOLDOWN);
        }), 5);
        add.add("upgrades usage 5", () -> command.accept("ta2 somnium level usage 5"), 10);
        add.add("upgrades check", () -> onServer(sp -> {
            TheabyssModVariables.PlayerVariables v = TheabyssModVariables.getPlayerVariables(sp);
            check("4 damage potions = level 2 and 1 / 4 towards level 3 (" + SomniumUpgrades.level(v, SomniumUpgrades.Kind.DAMAGE) + ", " + v.DamagePotions + ")",
                SomniumUpgrades.level(v, SomniumUpgrades.Kind.DAMAGE) == 2 && v.DamagePotions == 1);
            check("1 cooldown potion = level 1 (" + SomniumUpgrades.level(v, SomniumUpgrades.Kind.COOLDOWN) + ")", SomniumUpgrades.level(v, SomniumUpgrades.Kind.COOLDOWN) == 1);
            check("/ta2 somnium level usage 5 reaches the server (" + v.ManaUpgrade + ")", Math.abs(v.ManaUpgrade - 0.5) < 1.0E-6);
        }), 4);
        add.add("upgrades tooltip", () -> {
            List<String> lines = new java.util.ArrayList<>();
            ItemStack ring = new ItemStack(TheabyssModItems.RING_OF_CRYSTALS.get());
            ring.getItem().appendHoverText(ring, Item.TooltipContext.of(mc.level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT,
                line -> lines.add(line.getString()), net.minecraft.world.item.TooltipFlag.NORMAL);
            lines.forEach(line -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] tooltip | {}", line));
            String all = String.join(" | ", lines);
            check("tooltip shows the real Somnium cost with the base (usage 5: 6 (12))", all.contains("6 (12)"));
            check("tooltip shows the real slash damage (damage 2: 2.5 hearts, base 1.5)", all.contains("2.5 (1.5)"));
            check("locked needles are grey with the requirement", all.contains("Crystal Needles (Requires Damage level 3)"));
        }, 2);
        add.add("upgrades infinite", () -> command.accept("ta2 somnium set infinite"), 4);
        add.add("upgrades drain", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, vars -> vars.Mana = 10.0)), 4);
        add.add("upgrades infinite check", () -> onServer(sp -> check("infinite Somnium tops up (" + TheabyssModVariables.getPlayerVariables(sp).Mana + ")",
            TheabyssModVariables.getPlayerVariables(sp).Mana >= 99.9)), 4);
        add.add("upgrades finite", () -> command.accept("ta2 somnium set 100"), 6);
        add.add("gauntlet wheel down", () -> TheabyssModKeyMappings.SPELL_WHEEL.setDown(true), 3);
        add.add("gauntlet shot opening", () -> shot.accept("104_gauntlet_2a_wheel_opening"), 6);
        add.add("gauntlet shot wheel", () -> {
            check("the spell wheel opens while R is held", SpellWheelClient.isOpen());
            shot.accept("104_gauntlet_2_wheel");
        }, 3);
        add.add("gauntlet aim", () -> SpellWheelClient.aimForTest(new double[] {45.0, -20.0}), 3);
        add.add("gauntlet shot hover", () -> shot.accept("104_gauntlet_2b_wheel_hover"), 3);
        add.add("gauntlet aim damage", () -> SpellWheelClient.aimForTest(new double[] {-62.0, -62.0}), 3);
        add.add("gauntlet shot upgrade info", () -> shot.accept("104_gauntlet_2d_upgrade_info"), 3);
        add.add("gauntlet aim off", () -> SpellWheelClient.aimForTest(null), 2);
        add.add("gauntlet pick 2", () -> KeyMapping.click(mc.options.keyHotbarSlots[1].getKey()), 3);
        add.add("gauntlet shot closing", () -> shot.accept("104_gauntlet_2c_wheel_closing"), 3);
        add.add("gauntlet wheel up", () -> TheabyssModKeyMappings.SPELL_WHEEL.setDown(false), 8);
        add.add("gauntlet pick check", () -> {
            check("key 2 picks the crystals ring (client " + Gauntlet.selected(mc.player.getMainHandItem()) + ")",
                Gauntlet.selected(mc.player.getMainHandItem()) == 1 && !SpellWheelClient.isOpen());
            onServer(sp -> check("the server knows the pick (" + Gauntlet.selected(sp.getMainHandItem()) + ")", Gauntlet.selected(sp.getMainHandItem()) == 1));
        }, 5);
        add.add("gauntlet cast main", () -> mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND), 6);
        add.add("gauntlet main check", () -> onServer(sp -> {
            ItemStack hand = sp.getMainHandItem();
            check("right click casts the selected ring (crystals cooldown " + sp.getCooldowns().isOnCooldown(new ItemStack(TheabyssModItems.RING_OF_CRYSTALS.get())) + ")",
                sp.getCooldowns().isOnCooldown(new ItemStack(TheabyssModItems.RING_OF_CRYSTALS.get())));
            check("the hand still holds the gauntlet with its rings", Gauntlet.is(hand) && Gauntlet.rings(hand).get(1).is(TheabyssModItems.RING_OF_CRYSTALS.get())
                && Gauntlet.rings(hand).get(0).is(TheabyssModItems.RING_OF_FIRE.get()));
            double mana = TheabyssModVariables.getPlayerVariables(sp).Mana;
            check("the cast took Somnium (" + mana + ")", mana < manaBefore);
            manaBefore = mana;
        }), 4);
        add.add("gauntlet second locked", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 6);
        add.add("gauntlet second locked check", () -> onServer(sp -> {
            double mana = TheabyssModVariables.getPlayerVariables(sp).Mana;
            check("no needles below damage level 3 (" + mana + ")", mana == manaBefore);
        }), 2);
        add.add("gauntlet damage 3", () -> command.accept("ta2 somnium level damage 3"), 10);
        add.add("gauntlet cast second", () -> KeyMapping.click(TheabyssModKeyMappings.ABYSS_ECHOES.getKey()), 6);
        add.add("gauntlet second check", () -> onServer(sp -> {
            double mana = TheabyssModVariables.getPlayerVariables(sp).Mana;
            check("middle mouse casts the second magic (" + mana + ")", mana < manaBefore);
        }), 4);
        add.add("gauntlet shot badge", () -> shot.accept("104_gauntlet_3_badge"), 4);
        add.add("gauntlet result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT gauntlet {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] gauntlet check {}: {}", name, ok ? "ok" : "FAILED");
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
