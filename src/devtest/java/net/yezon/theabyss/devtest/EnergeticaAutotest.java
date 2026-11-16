package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity;
import net.yezon.theabyss.station.ArcanePower;
import net.yezon.theabyss.world.inventory.ArcaneMenu;

final class EnergeticaAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final BlockPos ARCANE = new BlockPos(0, 200, 0);

    private EnergeticaAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("en arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -8 199 -8 8 199 8 minecraft:grass_block");
            command.accept("execute in minecraft:overworld run fill -8 200 -8 8 208 8 minecraft:air");
            command.accept("time set noon");
            command.accept("gamemode survival @s");
            command.accept("clear @s");
            command.accept("execute in minecraft:overworld run setblock 0 200 0 theabyss:arcane_workbench");
            for (int[] c : ArcanePower.CORNERS) {
                command.accept("execute in minecraft:overworld run setblock " + c[0] + " 200 " + c[1] + " theabyss:abyssal_energetica");
            }
            for (int i = 0; i < 4; i++) {
                command.accept("item replace entity @s hotbar." + i + " with theabyss:loran_energy");
            }
        }, 30);
        for (int i = 0; i < 4; i++) {
            int corner = i;
            add.add("en aim " + i, () -> {
                int[] c = ArcanePower.CORNERS[corner];
                Vec3 target = new Vec3(c[0] + 0.5, 200.95, c[1] + 0.5);
                Vec3 stand = new Vec3(c[0] * 2 + 0.5, 200.0, c[1] * 2 + 0.5);
                Vec3 eye = stand.add(0.0, 1.62, 0.0);
                Vec3 d = target.subtract(eye);
                float yaw = (float)Math.toDegrees(Math.atan2(-d.x, d.z));
                float pitch = (float)-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
                command.accept("execute in minecraft:overworld run tp @s " + stand.x + " " + stand.y + " " + stand.z + " " + yaw + " " + pitch);
                mc.player.getInventory().setSelectedSlot(corner);
            }, 12);
            add.add("en charge " + i, () -> KeyMapping.click(mc.options.keyUse.getKey()), 8);
            if (i == 2) {
                add.add("en three", () -> onServer(sp -> {
                    check("three pillars charged (" + charged(sp) + ")", charged(sp) == 3);
                    fillRecipe(sp);
                    sp.openMenu((MenuProvider)sp.level().getBlockEntity(ARCANE), ARCANE);
                }), 10);
                add.add("en three result", () -> onServer(sp -> {
                    boolean empty = sp.containerMenu instanceof ArcaneMenu menu && menu.getSlot(9).getItem().isEmpty() && !menu.powered();
                    check("with three pillars the workbench does not work", empty);
                }), 2);
                add.add("en shot three", () -> shot.accept("110_energetica_1_three_gui"), 2);
                add.add("en close", () -> onServer(ServerPlayer::closeContainer), 4);
            }
        }
        add.add("en four", () -> onServer(sp -> {
            check("four pillars charged, the Loran Energy left the hotbar (" + charged(sp) + ")",
                charged(sp) == 4 && sp.getInventory().countItem(item("loran_energy")) == 0);
            check("the workbench is powered", ArcanePower.powered(sp.level(), ARCANE));
            sp.openMenu((MenuProvider)sp.level().getBlockEntity(ARCANE), ARCANE);
        }), 10);
        add.add("en four result", () -> onServer(sp -> check("with four pillars the recipe works (" + sp.containerMenu.getSlot(9).getItem() + ")",
            sp.containerMenu instanceof ArcaneMenu menu && menu.powered() && menu.getSlot(9).getItem().is(item("ring_of_spectral")))), 2);
        add.add("en shot four gui", () -> shot.accept("110_energetica_2_four_gui"), 2);
        add.add("en craft", () -> mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, 9, 0,
            net.minecraft.world.inventory.ContainerInput.QUICK_MOVE, mc.player), 6);
        add.add("en crafted", () -> onServer(sp -> {
            check("crafted the ring", sp.getInventory().countItem(item("ring_of_spectral")) == 1);
            boolean whole = true;
            for (int[] c : ArcanePower.CORNERS) {
                ItemStack e = pillar(sp, c).energy();
                whole &= e.is(item("loran_energy")) && e.getDamageValue() == 0;
            }
            check("the Loran Energy is not used up by crafting", whole && charged(sp) == 4);
            sp.closeContainer();
        }), 4);
        add.add("en overview", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 203.5 -5.5 0 35"), 30);
        add.add("en shot overview", () -> shot.accept("110_energetica_3_overview"), 2);
        add.add("en close up", () -> command.accept("execute in minecraft:overworld run tp @s -2.5 201.2 -4.5 -20 15"), 20);
        add.add("en shot close", () -> shot.accept("110_energetica_4_pillar"), 2);
        add.add("en night", () -> {
            command.accept("time set midnight");
            command.accept("execute in minecraft:overworld run tp @s 0.5 203.5 -5.5 0 35");
        }, 30);
        add.add("en shot night", () -> shot.accept("110_energetica_5_night"), 2);
        add.add("en aim back", () -> {
            command.accept("time set noon");
            Vec3 eye = new Vec3(-3.5, 201.62, -3.5);
            Vec3 d = new Vec3(-1.5, 200.95, -1.5).subtract(eye);
            float yaw = (float)Math.toDegrees(Math.atan2(-d.x, d.z));
            float pitch = (float)-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
            command.accept("execute in minecraft:overworld run tp @s -3.5 200 -3.5 " + yaw + " " + pitch);
            mc.player.getInventory().setSelectedSlot(5);
        }, 12);
        add.add("en take", () -> KeyMapping.click(mc.options.keyUse.getKey()), 8);
        add.add("en taken", () -> onServer(sp -> {
            check("an empty hand takes the Loran Energy back (" + charged(sp) + " charged, " + sp.getInventory().countItem(item("loran_energy")) + " in inventory)",
                charged(sp) == 3 && sp.getInventory().countItem(item("loran_energy")) == 1);
            check("the workbench is off again", !ArcanePower.powered(sp.level(), ARCANE));
        }), 2);
        add.add("en result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT energetica {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static net.minecraft.world.item.Item item(String id) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("theabyss", id));
    }

    private static AbyssalEnergeticaBlockEntity pillar(ServerPlayer sp, int[] c) {
        return (AbyssalEnergeticaBlockEntity)sp.level().getBlockEntity(ARCANE.offset(c[0], 0, c[1]));
    }

    private static int charged(ServerPlayer sp) {
        int n = 0;
        for (int[] c : ArcanePower.CORNERS) {
            if (sp.level().getBlockEntity(ARCANE.offset(c[0], 0, c[1])) instanceof AbyssalEnergeticaBlockEntity p && p.charged()) {
                n++;
            }
        }
        return n;
    }

    private static void fillRecipe(ServerPlayer sp) {
        Container c = (Container)sp.level().getBlockEntity(ARCANE);
        c.setItem(0, new ItemStack(item("ring_of_invisibility")));
        for (int i = 1; i <= 8; i++) {
            c.setItem(i, new ItemStack(item(i % 2 == 1 ? "phantom_essence" : "hollow_powder")));
        }
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] energetica check {}: {}", name, ok ? "ok" : "FAILED");
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
