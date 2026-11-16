package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

final class VillageAutotest {
    private VillageAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("village spectator", () -> {
            command.accept("gamemode spectator @s");
            command.accept("time set noon");
            setHudHidden(mc, true);
        }, 20);
        views(add, command, shot, "theabyss:the_abyss", -736, 304, "80_abyss_village");
        views(add, command, shot, "minecraft:overworld", -736, 304, "81_overworld_village");
        add.add("village gui back", () -> setHudHidden(mc, false), 5);
    }

    private static void setHudHidden(Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden) {
            mc.gui.hud.toggle();
        }
    }

    private static void views(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot, String dim, int x, int z, String prefix) {
        String at = "execute in " + dim + " positioned " + x + " 0 " + z + " positioned over world_surface run ";
        add.add(prefix + " load", () -> command.accept("execute in " + dim + " run forceload add " + (x - 64) + " " + (z - 64) + " " + (x + 64) + " " + (z + 64)), 40);
        add.add(prefix + " go", () -> command.accept(at + "tp @s ~-26 ~22 ~-26 facing ~ ~ ~"), 400);
        add.add(prefix + " shot sw", () -> shot.accept(prefix + "_1_sw"), 20);
        add.add(prefix + " ne", () -> command.accept(at + "tp @s ~30 ~18 ~30 facing ~ ~ ~"), 160);
        add.add(prefix + " shot ne", () -> shot.accept(prefix + "_2_ne"), 20);
        add.add(prefix + " top", () -> command.accept(at + "tp @s ~ ~55 ~-34 facing ~ ~ ~"), 160);
        add.add(prefix + " shot top", () -> shot.accept(prefix + "_3_top"), 20);
        add.add(prefix + " street", () -> command.accept(at + "tp @s ~-12 ~3 ~-12 facing ~ ~2 ~"), 160);
        add.add(prefix + " shot street", () -> shot.accept(prefix + "_4_street"), 20);
    }
}
