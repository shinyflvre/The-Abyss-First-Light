package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

final class HandLightAutotest {
    private HandLightAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("handlight arena", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.COLORED_LIGHT_ENGINE.set(false);
            command.accept("execute in minecraft:overworld run forceload add -12 -12 12 12");
            command.accept("execute in minecraft:overworld run fill -12 199 -12 12 199 12 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -12 200 -12 12 210 12 minecraft:air");
            command.accept("time set midnight");
            command.accept("weather clear");
            command.accept("gamemode creative @s");
            command.accept("clear @s");
        }, 60);
        add.add("handlight goto", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200 -1.5 0 30"), 80);
        add.add("handlight scene", () -> {
            command.accept("fill -6 199 0 6 199 1 theabyss:dirt");
            String[] plants = {"blue_cave_shroom", "glow_shroom", "ralom_plant", "rub_shroom", "toxic_sun_flower", "exolius_plant", "hollow_crystal_ore"};
            for (int k = 0; k < plants.length; k++) {
                command.accept("setblock " + (-6 + k * 2) + " 200 1 theabyss:" + plants[k]);
                command.accept("setblock " + (-5 + k * 2) + " 200 1 theabyss:" + plants[k]);
            }
            command.accept("fill -8 200 3 8 205 3 minecraft:stone");
            command.accept("item replace entity @s weapon.mainhand with minecraft:orange_terracotta");
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }, 60);
        add.add("handlight shot off", () -> shot.accept("130_handlight_engine_off"), 10);
        add.add("handlight engine on", () -> net.yezon.theabyss.configuration.ConfigConfiguration.COLORED_LIGHT_ENGINE.set(true), 120);
        add.add("handlight shot on", () -> shot.accept("130_handlight_engine_on"), 10);
        add.add("handlight state", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT handlight light engine active {}",
            net.yezon.theabyss.client.fx.ColoredLight.active()), 2);
        add.add("handlight reset", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.COLORED_LIGHT_ENGINE.set(false);
            command.accept("clear @s");
        }, 4);
    }
}
