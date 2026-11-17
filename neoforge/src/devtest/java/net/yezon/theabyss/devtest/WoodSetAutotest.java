package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

final class WoodSetAutotest {
    private static final String[] WOODS = {"blaru", "nightgrove", "bog", "sal", "slimed", "frozen", "rena", "luna", "vigilant"};

    private WoodSetAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("wood load", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -24 48 24");
            command.accept("time set noon");
            command.accept("gamemode spectator @s");
        }, 40);
        add.add("wood platform", () -> {
            command.accept("execute in minecraft:overworld run fill -4 199 -12 40 199 8 minecraft:smooth_stone");
            command.accept("execute in minecraft:overworld run fill -4 200 -12 40 210 8 minecraft:air");
            command.accept("execute in minecraft:overworld run kill @e[type=!minecraft:player,x=-4,y=195,z=-12,dx=44,dy=20,dz=20]");
        }, 20);
        add.add("wood sets", () -> {
            for (int i = 0; i < WOODS.length; i++) {
                String w = WOODS[i];
                int x = i * 4;
                String in = "execute in minecraft:overworld run ";
                command.accept(in + "setblock " + x + " 200 0 theabyss:" + w + "_sign[rotation=8]");
                command.accept(in + "setblock " + x + " 203 0 theabyss:" + w + "_planks");
                command.accept(in + "setblock " + x + " 202 0 theabyss:" + w + "_hanging_sign[rotation=8]");
                command.accept(in + "setblock " + x + " 200 3 theabyss:" + w + "_shelf[facing=north]");
                command.accept(in + "item replace block " + x + " 200 3 container.0 with theabyss:" + w + "_sign");
                command.accept(in + "item replace block " + x + " 200 3 container.1 with theabyss:" + w + "_hanging_sign");
                command.accept(in + "item replace block " + x + " 200 3 container.2 with theabyss:" + w + "_boat");
                command.accept(in + "summon theabyss:" + w + "_boat " + x + ".5 200 -4 {Rotation:[90f,0f]}");
                command.accept(in + "summon theabyss:" + w + "_chest_boat " + x + ".5 200 -8 {Rotation:[90f,0f]}");
            }
        }, 60);
        add.add("wood hud", () -> {
            if (!mc.options.hideGui) {
                mc.options.hideGui = !mc.options.hideGui;
            }
        }, 5);
        add.add("wood view all", () -> command.accept("execute in minecraft:overworld run tp @s 16 207 -18 facing 16 200 1"), 200);
        add.add("wood shot all", () -> shot.accept("90_woodsets_all"), 20);
        add.add("wood view signs", () -> command.accept("execute in minecraft:overworld run tp @s 8 202 -3 facing 8 201 2"), 80);
        add.add("wood shot signs", () -> shot.accept("90_woodsets_signs_left"), 20);
        add.add("wood view signs 2", () -> command.accept("execute in minecraft:overworld run tp @s 24 202 -3 facing 24 201 2"), 80);
        add.add("wood shot signs 2", () -> shot.accept("90_woodsets_signs_right"), 20);
        add.add("wood view boats", () -> command.accept("execute in minecraft:overworld run tp @s 16 205 -14 facing 16 200 -6"), 80);
        add.add("wood shot boats", () -> shot.accept("90_woodsets_boats"), 20);
        add.add("wood hud back", () -> mc.options.hideGui = !mc.options.hideGui, 5);
    }
}
