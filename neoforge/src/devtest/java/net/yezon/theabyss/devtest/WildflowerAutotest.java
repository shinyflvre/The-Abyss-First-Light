package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

final class WildflowerAutotest {
    private WildflowerAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("wf setup", () -> {
            command.accept("execute in theabyss:the_abyss run forceload add -16 1560 64 1640");
            command.accept("execute in theabyss:the_abyss run forceload add 0 0 80 80");
            command.accept("gamemode spectator @s");
            command.accept("effect give @s minecraft:night_vision infinite 0 true");
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
        }, 40);
        add.add("wf duskveil", () -> command.accept("execute in theabyss:the_abyss run tp @s 27.5 76 1592.5 facing 27.5 68 1602.5"), 300);
        add.add("wf shot duskveil", () -> shot.accept("96_duskveil_blossoms"), 20);
        add.add("wf starpetals", () -> command.accept("execute in theabyss:the_abyss run tp @s 40.5 112 25.5 facing 40.5 104 35.5"), 300);
        add.add("wf shot starpetals", () -> shot.accept("96_blaru_starpetals"), 20);
        add.add("wf hud back", () -> mc.gui.hud.toggle(), 5);
    }
}
