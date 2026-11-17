package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

final class DripstoneAutotest {
    private DripstoneAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("drip setup", () -> {
            command.accept("execute in theabyss:the_abyss run forceload add -16 56 64 128");
            command.accept("gamemode spectator @s");
            command.accept("effect give @s minecraft:night_vision infinite 0 true");
            if (!mc.options.hideGui) {
                mc.options.hideGui = !mc.options.hideGui;
            }
        }, 40);
        int[] yaws = {0, 90, 180, 270};
        for (int yaw : yaws) {
            add.add("drip look " + yaw, () -> command.accept("execute in theabyss:the_abyss run tp @s 19.5 30 87.5 " + yaw + " -25"), yaw == 0 ? 300 : 60);
            add.add("drip shot " + yaw, () -> shot.accept("95_dripstone_" + yaw), 20);
        }
        add.add("drip hud back", () -> mc.options.hideGui = !mc.options.hideGui, 5);
    }
}
